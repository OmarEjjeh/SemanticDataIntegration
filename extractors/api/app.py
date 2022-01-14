from flask import Flask, jsonify
from flask import request
import requests
import os
import pandas as pd
import re
import json
import spacy
from pathlib import Path
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry
import math
import tika
tika.initVM()
from tika import parser
from tika import language
import cld3
import datetime

from flask_restful import Resource, Api
from apispec import APISpec
from marshmallow import Schema, fields
from apispec.ext.marshmallow import MarshmallowPlugin
from flask_apispec.extension import FlaskApiSpec
from flask_apispec.views import MethodResource
from flask_apispec import marshal_with, doc, use_kwargs

from duckdq.checks import Check, CheckLevel
from duckdq.verification_suite import VerificationSuite

query_instanceof = 'SELECT%20%3Fitem%20%3FitemLabel%0AWHERE%20%0A%7B%0A%20%20wd%3A{}%20wdt%3AP31%20%3Fitem.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D'
sparql_url = 'https://query.wikidata.org/sparql?query={}&format=json'

nlp = spacy.load('en_core_web_lg')

app = Flask(__name__)
api = Api(app)
app.config.update({
    'APISPEC_SPEC': APISpec(
        title='Metadata Extraction Project',
        version='v1',
        plugins=[MarshmallowPlugin()],
        openapi_version='2.0.0'
    ),
    'APISPEC_SWAGGER_URL': '/swagger/',  # URI to access API Doc JSON
    'APISPEC_SWAGGER_UI_URL': '/swagger-ui/'  # URI to access UI of API Doc
})
docs = FlaskApiSpec(app)

class Key_word_response_schema(Schema):
    success = fields.Bool(default=True)
    data = fields.List(fields.Str(),default=['keyword1','keyword2','....'])

class Key_word_request_schema(Schema):
    link = fields.String(required=True, description="link to the csv file", default='True')

class data_quality_response_schema_substructure(Schema):
    file_quality_score = fields.Float(default='100')
    file_quality = fields.String()
    percentage_missing = fields.Float(default='100')
    percentNA = fields.Float(default='100')
class data_quality_response_schema(Schema):
    success = fields.Bool(default=True)
    data = fields.Nested(data_quality_response_schema_substructure, metadata={"file_quality_score": "file_quality_score", "file_quality": "file_quality","percentage_missing":"percentage_missing","percentNA":"percentNA"})
    
class data_quality_request_schema(Schema):
    link = fields.String(required=True, description="Extracts data quality")

class language_response_schema_substructure(Schema):
    language = fields.Str(default='English')
    language_probablity = fields.Float(default='100')

class language_response_schema(Schema):
    success = fields.Bool(default=True)
    data = fields.Nested(language_response_schema_substructure, metadata={"description": "Nested 1", "title": "Title1"})
    
class language_request_schema(Schema):
    link = fields.String(required=True, description="Extracts language")

class date_response_schema_substructure(Schema):
    start_date = fields.String()
    end_date = fields.String()

class date_response_schema(Schema):
    success = fields.Bool(default=True)
    data = fields.Nested(date_response_schema_substructure, metadata={"start_date": "Date", "end_date": "date"})
    
class date_request_schema(Schema):
    link = fields.String(required=True, description="Extracts date-time")

class file_meta_data_request_schema(Schema):
    link = fields.String(required=True, description="Extracts data quality")

class file_meta_data_response_schema_substructure(Schema):
    file_name = fields.String(required=True, description="file name meta data")                                         
    file_type = fields.String(required=True, description="file type meta data")
    creation_date = fields.String(required=True, description="creation date of the file")
    file_size = fields.String(required=True, description="file size of the file")

class file_meta_data_response_schema(Schema):
    success = fields.Bool(default=True)
    data = fields.Nested(file_meta_data_response_schema_substructure, metadata={"file_name": "file name", "file_type": "file type", "creation_date":"creation date", "file_size":"file size"})

def remove_URL_location(dataframe):
    regex_url_expression = '(http|ftp|https)://([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])?'
    regex_location_expression = '^\([+-]?([1-8]?\d(\.\d+)?|90(\.0+)?), [+-]?((1[0-7]|[1-9])?\d(\.\d+)?|180(\.0+)?)\)$'
    remove_cols = []
    for col in dataframe:
        each_col_data = dataframe.loc[dataframe[col].notnull()]
        data = each_col_data.sample()[col].values[0]
        links = re.findall(regex_url_expression, str(data))
        if(len(links) > 0):
            remove_cols.append(col)
        locations = re.findall(regex_location_expression, str(data))
        if(len(locations) > 0):
            remove_cols.append(col)
        if(bool(re.search(r'\d', data))):
            remove_cols.append(col)
    dataframe.drop(remove_cols, inplace=True, axis=1)
    return dataframe

def get_target_colums(dataframe):
    #Remove all the numeric data types and select only those of type object
    dataframe = dataframe.loc[:, dataframe.dtypes == object]

    #Convert the data-time object which was previously unrecognised to type data type and select those with object
    dataframe = dataframe.apply(lambda col: pd.to_datetime(col, errors='ignore') 
              if col.dtypes == object 
              else col, 
              axis=0)
    dataframe = dataframe.loc[:, dataframe.dtypes == object]

    #remove the colums with URLs 
    dataframe = remove_URL_location(dataframe)

    return dataframe

def get_keywords(dataframe):
    keywords = {}
    keywords_per_column = {}
    for col in dataframe:
        keywords_per_column[col] = []
        counts = dataframe[col].value_counts(normalize=True).to_dict()
        values = [v for k,v in counts.items()]
        mean = sum(values)/len(values)
        for k,v in counts.items():
            if(v >= mean):
                keywords[k] = v
                keywords_per_column[col].append(k)
    return keywords, keywords_per_column

def get_wikidataIDs(keyword_list):
    headers = {
        'Content-Type': 'application/json',
    }
    params = (
    ('mode', 'long'),
    )

    s = requests.Session()
    retries = Retry(total=5, backoff_factor=1, status_forcelist=[500, 502, 503, 504 ])
    s.mount('http://', HTTPAdapter(max_retries=retries))

    wikidata_ids = []
    wikidata_ids_map = {}
    for key in keyword_list:
        data = {"text":key}
        data = json.dumps(data)
        response = s.post('https://labs.tib.eu/falcon/falcon2/api', headers=headers, params=params, data=data)
        if(response.status_code == 200):
            data = response.text
            data = json.loads(data)
            entities_data = data['entities_wikidata']
            if(entities_data !=[] and entities_data[0]!=[]):
                wikidata_link = entities_data[0][0]
                wikidata_id = wikidata_link.split("/")[-1].split(">")[0]
                wikidata_ids.append(wikidata_id)
                wikidata_ids_map[wikidata_id] = key
    return wikidata_ids, wikidata_ids_map

def fetch_query_results(query_url):
    headers = {
        'User-Agent': 'RelationExtractionKAA/0.0 (ansonbastos@gmail.com) UsedBaseLibrary/0.0',
    }
    s = requests.Session()
    retries = Retry(total=5, backoff_factor=1, status_forcelist=[500, 502, 503, 504 ])
    s.mount('http://', HTTPAdapter(max_retries=retries))
    response = s.get(query_url,headers=headers)
    instance_of_data = []
    if(response.status_code == 200):
        data = response.text
        data = json.loads(data)
        if(data['results']):
            if(data['results']['bindings'] and len(data['results']['bindings']) > 0):
                for i in data['results']['bindings']:
                    if(i['itemLabel']):
                        if(i['itemLabel']['xml:lang'] == 'en'):
                            instance_of_data.append(i['itemLabel']['value'])
            else:
                return instance_of_data
        else:
            return instance_of_data 
    return instance_of_data

def get_instance_of_values(wikidata_ids):
    instance_of = []
    instance_of_map = {}
    for wiki_id in wikidata_ids:
        instance_of_id = fetch_query_results(sparql_url.format(query_instanceof.format(wiki_id)))
        instance_of += instance_of_id
        instance_of_map[wiki_id] = instance_of_id
    unique_value_instance = list(set(instance_of))
    if('Wikimedia disambiguation page' in unique_value_instance):
        unique_value_instance.remove('Wikimedia disambiguation page')
    return unique_value_instance, instance_of_map

def most_common(lst):
    if(len(lst) > 0):
        return max(set(lst), key=lst.count)
    return []

def most_common_spacy(words, instance_of):
    score = {}
    for i in instance_of:
        i = nlp(i)
        sum = 0
        for w in words:
            w = nlp(w)
            sum += w.similarity(i)
        score[i.text] = (sum/len(words))
    print("SCORE >>>>> ",score)
    max_key = max(score, key=score.get)
    print("MAX SCORE>> ",max_key," | ",score[max_key])
    return max_key

def get_instance_of_values_per_column(keywords_per_column, wikidata_ids_map, instance_of_map):
    wikidata_ids_inv_map = {v: k for k, v in wikidata_ids_map.items()}
    instance_of_per_col = {}
    final_values = {}
    for key,values in keywords_per_column.items():
        instance_of_per_col[key] = []
        for word in values:
            if(word in wikidata_ids_inv_map):
                wiki_id = wikidata_ids_inv_map[word]
                instance_of = instance_of_map[wiki_id]
                instance_of_per_col[key] += instance_of
        print("========================================",key)
        print("words per col >>>>>>>>>>>>> ",len(values))
        print("instance_of_per_col >>>>>>> ",len(instance_of_per_col[key]))
        if(len(instance_of_per_col[key]) > 0):
            if('Wikimedia disambiguation page' in instance_of_per_col[key]):
                instance_of_per_col[key] = list(filter(lambda a: a != 'Wikimedia disambiguation page', instance_of_per_col[key]))
            final_values[key] = most_common_spacy(values,instance_of_per_col[key])
            print("FREQUENT >>>>>>> ",most_common_spacy(values,instance_of_per_col[key]))
            print("MOST COMMON >> ",most_common(instance_of_per_col[key]))
        else:
            print("FREQUENT >>>>>>> 0")
        print("========================================",key)
    print("instance_of_per_col >>> ",instance_of_per_col)
    all_keywords = list(final_values.values())
    return all_keywords

def download_file(url: str, dest_folder = "downloads"):
    if not os.path.exists(dest_folder):
        os.makedirs(dest_folder)  

    filename = url.split('/')[-1].replace(" ", "_")  # be careful with file names
    file_path = os.path.join(dest_folder, filename)

    r = requests.get(url, stream=True)
    if r.ok:
        print("saving to", os.path.abspath(file_path))
        with open(file_path, 'wb') as f:
            for chunk in r.iter_content(chunk_size=1024 * 8):
                if chunk:
                    f.write(chunk)
                    f.flush()
                    os.fsync(f.fileno())
    else:  # HTTP status code 4XX/5XX
        print("Download failed: status code {}\n{}".format(r.status_code, r.text))
        return None
    file_path = os.path.abspath(file_path)
    return file_path


def error_response():
    return {"success":False,"data":{"error":"Error in downloading the data"}}

def check_for_file(url: str, dest_folder = "downloads"):
    filename = url.split('/')[-1].replace(" ", "_")
    file_path = os.path.join(dest_folder, filename)
    my_file = Path(file_path)
    if (my_file.is_file()):
        return file_path
    else:
        return None

def calculate_data_quality(file_path):

    df = pd.read_csv(file_path, error_bad_lines=False)
    #df.columns = [col[1:-1] for col in df.columns]
    df = df.replace('"', '', regex=True)
    original_cols = df.columns.tolist()
    new_cols = {}
    for elem in original_cols:
        ret = elem
        ret = ret.strip()
        ret = ret.strip('"')
        ret = ret.strip('.')
        ret = ret.strip('_')
        if '"' in ret:
            ret = ret.replace('"', '')
        if '/' in ret:
            ret = ret.replace('/', '_')
        if ' ' in ret:
            ret = ret.replace(' ', '_')
        new_cols[elem] = ret
    df.rename(columns=new_cols, inplace=True)
    with open(file_path, 'r') as fid:
        # header_list = fid.readline().strip().split(",")
        cNaN = fid.read().count('NaN')

    header_list = df.columns.tolist()
    cHeader = len(header_list)
    
    cBlank = 0
    for row in df.values:
        for value in row:
            if type(value)==float:
                if math.isnan(value):
                    cBlank+=1
    
    base_obj = Check(CheckLevel.EXCEPTION, "Basic Check")
    given_function = getattr(base_obj, "has_size")(lambda s: s > 0)
        
    i=0
    for i in range(0,len(header_list)):
        given_function = getattr(given_function, "is_complete")(header_list[i])
        given_function = getattr(given_function, "is_unique")(header_list[i])
        #given_function = getattr(given_function, "hasEntropy,_> 0.99")(header_list[i])

    try:
        verification_result = (
            VerificationSuite()
                .on_data(df, dataset_id="data10", partition_id="1")
               # .using_metadata_repository("duckdb://basic_check.duckdq")
                .add_check(
                    given_function
            )
            .run()
        )
    except:
        return(-1,"Couldn't be calculated")
    
    Nnull=Uniq=0
    
    for check, check_result in verification_result.check_results.items():
        for constraint_result in check_result.constraint_results:
            constraint_s = constraint_result.constraint.__str__()
            constraint_result_s = constraint_result.status.name
            metric_value = constraint_result.metric.value
            metric_s = metric_value.get() if metric_value.isSuccess else metric_value
            column = constraint_result.metric.instance
            name    = constraint_result.metric.name
            if name == "Size":
                cRows = metric_s
                print("The data file contains",cRows, "rows")
            elif name == "Completeness":
                Nnull = Nnull + round(metric_s*100,2)
            elif name == "Uniqueness":
                Uniq = Uniq + round(metric_s*100,2)
                
    file_quality_score = round(80/100*(Nnull/cHeader)+20/100*(Uniq/cHeader)-10/100*(cNaN+cBlank)/(cHeader * cRows),2)

    percentage_missing = round((cNaN)/ (cHeader*cRows)*100,2)
    percentNA = round((cBlank)/ (cHeader*cRows)*100,2)
    
    print("percentage_missing >> ",percentage_missing)
    print("percentNA >> ",percentNA)
    
    if file_quality_score >= 85:
        file_quality = "excellent"
    elif file_quality_score >= 55:
        file_quality = "good"
    elif file_quality_score >= 30:
        file_quality = "sufficient"
    elif file_quality_score < 30:
        file_quality = "bad"

    return(file_quality_score,file_quality,percentage_missing,percentNA)
    

class KeyWord(MethodResource, Resource):
    @doc(description='My First KeyWord  API.', tags=['KeyWord'])
    @use_kwargs(Key_word_request_schema, location=('json'))
    @marshal_with(Key_word_response_schema)  # marshalling
    def post(self, link):
        print("\n\n============================== KEYWORD API START ============================")
        if not request.json or not 'link' in request.json:
            abort(400)
    
        file_path = download_file(request.json['link'])
    
        if(file_path is None):
            return error_response()
        print("file_path >>>>>>>>>> ",file_path)
        df = pd.read_csv(file_path,error_bad_lines=False)
        df = get_target_colums(df)
        keywords_dict, keywords_per_column= get_keywords(df)
        keywords = [k for k,v in keywords_dict.items()]
        wikidata_ids, wikidata_ids_map = get_wikidataIDs(keywords)
        instance_of, instance_of_map = get_instance_of_values(wikidata_ids)
        keywords = get_instance_of_values_per_column(keywords_per_column, wikidata_ids_map, instance_of_map)
        print("OUTPUT OF KEYWORD API >>> ",keywords)
        print("============================== KEYWORD API END ============================")
        return {"success":True,"data":keywords}

class DataQuality(MethodResource, Resource):
    @doc(description='My First DataQuality API.', tags=['DataQuality'])
    @use_kwargs(data_quality_request_schema, location=('json'))
    @marshal_with(data_quality_response_schema)  # marshalling
    def post(self, link):
        print("\n\n============================== DATAQUALITY API START ============================")
        if not request.json or not 'link' in request.json:
            abort(400)
    
        file_path = check_for_file(request.json['link']) 
        if(file_path == None):
            file_path = download_file(request.json['link'])
        
        if(file_path is None):
            return error_response()
    
        file_quality_score,file_quality,percentage_missing,percentNA = calculate_data_quality(file_path)
        print("OUTPUT OF DATAQUALITY API >>>>>> ",file_quality_score,file_quality,percentage_missing,percentNA)
        print("============================== DATAQUALITY API END ============================")
        return {"success":True,"data":{"file_quality_score":file_quality_score,
                                                "file_quality":file_quality,
                                                "percentage_missing":percentage_missing,
                                                "percentNA":percentNA}}

class Language(MethodResource, Resource):
    @doc(description='My First Language API.', tags=['Language'])
    @use_kwargs(language_request_schema, location=('json'))
    @marshal_with(language_response_schema)  # marshalling
    def post(self, link):
        print("\n\n============================== LANGUAGE API START ============================")
        if not request.json or not 'link' in request.json:
            abort(400)
    
        file_path = check_for_file(request.json['link']) 
        if(file_path == None):
            file_path = download_file(request.json['link'])
    
        if(file_path is None):
            return error_response()
    
        parsed = parser.from_file(file_path)
        predicted_lang = list(cld3.get_language(parsed['content']))
    
        language = predicted_lang[0]
        language_probablity = 100*round(predicted_lang[1],2)
        print("OUTPUT OF LANGUAGE API >>> ",language,language_probablity)
        print("============================== LANGUAGE API END ============================")
        return {"success":True,"data":{"language":language,
                                                "language_probablity":language_probablity}}

class DateTime(MethodResource, Resource):
    @doc(description='My First DateTime API.', tags=['DateTime'])
    @use_kwargs(date_request_schema, location=('json'))
    @marshal_with(date_response_schema)  # marshalling
    def post(self, link):
        print("\n\n============================== DATETIME API START ============================")
        if not request.json or not 'link' in request.json:
            abort(400)
    
        file_path = check_for_file(request.json['link']) 
        if(file_path == None):
            file_path = download_file(request.json['link'])
    
        if(file_path is None):
            return error_response()
    
        df = pd.read_csv(file_path, error_bad_lines=False)
        df_object_columns = df[df.select_dtypes(['object']).columns]
    
        for col in df_object_columns.columns:
            try: 
                df_object_columns[col]=df_object_columns[col].apply(pd.to_datetime)
            except:
                del df_object_columns[col]
    
        if (len(df_object_columns.columns)== 0):
            return {"success":True,"data":{"start_date":None,
                                                "end_date":None}}
    
        else:
            start_date = min(list(df_object_columns.min()))
            end_date = max(list(df_object_columns.max()))
            print("OUTPUT OF DATETIME API >>> ",start_date,end_date)
            print("============================== DATETIME API END ============================")
            return {"success":True,"data":{"start_date":start_date.isoformat(),
                                                "end_date":end_date.isoformat()}}

class FileMetaData(MethodResource, Resource):
    @doc(description='My First FileMetaData API.', tags=['FileMetaData'])
    @use_kwargs(date_request_schema, location=('json'))
    @marshal_with(file_meta_data_response_schema)  # marshalling
    def post(self, link):
        print("\n\n============================== FILE META DATA API START ============================")
        if not request.json or not 'link' in request.json:
            abort(400)
    
        file_path = check_for_file(request.json['link']) 
        if(file_path == None):
            file_path = download_file(request.json['link'])
    
        if(file_path is None):
            return error_response()

        parsed = parser.from_file(file_path)
        metadata = parsed["metadata"]
        filename = metadata['resourceName']
        filetype = metadata['Content-Type'].split(';')[0]
        filesize = os.stat(file_path).st_size

        if 'Creation-Date' in metadata:
            creation_date = metadata['Creation-Date']
        else:
            creation_date = datetime.datetime.today().isoformat()
        print("OUTPUT OF FILE META DATA API >> ",filename,filetype,creation_date,filesize)
        print("============================== FILE META DATA API END ============================")
        return{"success":True,"data":{"file_name":filename,
                                        "file_type":filetype,
                                        "creation_date":creation_date,
                                        "file_size":filesize}}


    


api.add_resource(KeyWord, '/key_word')
docs.register(KeyWord)
api.add_resource(DataQuality, '/data_quality')
docs.register(DataQuality)
api.add_resource(Language, '/language')
docs.register(Language)
api.add_resource(DateTime, '/date_time')
docs.register(DateTime)
api.add_resource(FileMetaData, '/file_meta_data')
docs.register(FileMetaData)
if __name__ == '__main__':
    app.run(debug=True)

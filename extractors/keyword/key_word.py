import pandas as pd
import re
import requests
import json
import spacy
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

query_instanceof = 'SELECT%20%3Fitem%20%3FitemLabel%0AWHERE%20%0A%7B%0A%20%20wd%3A{}%20wdt%3AP31%20%3Fitem.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D'
sparql_url = 'https://query.wikidata.org/sparql?query={}&format=json'

nlp = spacy.load('en_core_web_lg')

  
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
	return 0
def remove_URL_location(dataframe):
	regex_url_expression = '(http|ftp|https)://([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])?'
	regex_location_expression = '^\([+-]?([1-8]?\d(\.\d+)?|90(\.0+)?), [+-]?((1[0-7]|[1-9])?\d(\.\d+)?|180(\.0+)?)\)$'
	remove_cols = []
	for col in dataframe:
		each_col_data = df.loc[df[col].notnull()]
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
		counts = df[col].value_counts(normalize=True).to_dict()
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

def get_instance_of_values_per_column(keywords_per_column, wikidata_ids_map, instance_of_map):
	wikidata_ids_inv_map = {v: k for k, v in wikidata_ids_map.items()}
	instance_of_per_col = {}
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
			print("FREQUENT >>>>>>> ",most_common_spacy(values,instance_of_per_col[key]))
			print("MOST COMMON >> ",most_common(instance_of_per_col[key]))
		else:
			print("FREQUENT >>>>>>> 0")
		print("========================================",key)
	print("instance_of_per_col >>> ",instance_of_per_col)
	return None

 #################  MAIN #############################
#file_name = 'Trees_In_Camden.csv'
#file_name = 'google_activity_by_London_Borough.csv'
#file_name = 'scotland_animal.csv'
#file_name = 'Recycling_Centres.csv'
file_name = 'Active_Projects_Under_Construction.csv'
#file_name = 'FeedGrains.csv'
df = pd.read_csv(file_name)
#df = pd.read_csv('')

#print("BEFORE >>> ", df.dtypes)
df = get_target_colums(df)
#print("AFTER >>> ", get_target_colums(df).dtypes)
keywords_dict, keywords_per_column= get_keywords(df)
keywords = [k for k,v in keywords_dict.items()]
print("keywords_per_column >> ",keywords_per_column)
wikidata_ids, wikidata_ids_map = get_wikidataIDs(keywords)
instance_of, instance_of_map = get_instance_of_values(wikidata_ids)
get_instance_of_values_per_column(keywords_per_column, wikidata_ids_map, instance_of_map)
df1 = pd.DataFrame(list(wikidata_ids_map.items()), columns=['wiki_id', 'Key_words'])
df2 = pd.DataFrame(list(instance_of_map.items()), columns=['wiki_id', 'Instance_of'])
df3 = pd.merge(df1, df2, on="wiki_id")
save_file_name = file_name.split(".")[0] + "_META_DATA.csv" 
df3.to_csv(save_file_name)
print("instance_of >>> ",instance_of)



# Metadata Extraction Project

The MDE project is built around an RDF triplestore which stores metadata concerning datasets.
End users can query the database in order to find datasets the
metadata of whih satisfies certain constraints (e.g. quality constraints,
constraints on the temporal context etc.). Admins can introduce new datasets
into the system by uploading a URL linking to the dataset (right now, only CSV files are suipported). 
The system then automatically extracts certain metadata
concerns and creates a knowledge graph using the IDS Information Model as ontology and stores the metadata in the 
database for future lookups. 

## Main Software Components

The project consists of a frontend, a backend and the extractor. The frontend accepts user requests, the backend manages the database and processes user requests and the extractor analyzes the datasets to retrieve the metadata.

### Metadata Extractor component

The extractor is written in Python 3. Its task is to extract as many metadata
concerns as possible from "naked" data, i.e. data that isn't already annotated
with metadata.

To run the extractor componets, first create the virtual environment and activate the environment:
```
1. pip3 install virtualenv
2. python3 -m virtualenv venv
3. source venv/bin/activate

```

The following commands might be necessary to install required dependencies:
```
1. cd extractors/api/installation
2. pip install -r requirements.tx
3. unzip duckdq.zip 
4. cd duckdq
5. python setup.py install

```

The extractor can be run locally with the following command:
```
1. cd extractors/api
2. python app.py

```
The server will run by default on ``http://127.0.0.1:5000/``
The API documentation and the swagger can be found at ``http://127.0.0.1:5000/swagger-ui/``

### Backend

The backend is written in Java 11 using Spring Boot. By default, it runs on ``http://localhost:8080`` To build, run

```
mvn clean install 
```

Afterwards, run the jar file in the ``target`` folder.

The backend provides three API endpoints:
``/query/``,  ``/submit/`` and ``/download/``. The query endpoint  can process GET requests where the search criteria
are sent using query parameters. It returns a JSON file which contains a list of
JSON objects representing datasets with their appropriate metadata. The
submit endpoint processes POST requests sent by the user. These requests contain a title, description and URL to the csv file in ther request body. The URL is sent to the extractor, which
returns a JSON document containing the metadata concerns. These metadata
concerns are cast into the IDS IM and then written to the embedded database. The download endpoint receives a GET request with the url of a dataset that has already been added to the database and retrieves the relevant metadata file.

For the APIs, documentation can be found under ``http://localhost:8080/swagger-ui/``. 

### Frontend

The frontend is written in the React framework. By default, it runs on ``http://localhost:3000``. It communicates with the backend to process user queries and uploads. 

To install it, run the following commands:
```
1. cd frontend
2. npm install
3. npm start run 

```


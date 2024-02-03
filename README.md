original source code: https://github.com/daseECNU/Touchstone
Based on original source code, added 
1. query analyzer
2. handle more data constraints (inclusion constraints, format constraints, etc.)
3. handle circular dependency 

thesis: https://www2.eecs.berkeley.edu/Pubs/TechRpts/2023/EECS-2023-124.html 
thesis evaluation is deprecated for now because later on more features was added. 

project structure break down: 
1. query analyzer rules are described in the thesis. query analyzer code is under the directory /src/edu/ecnu/touchstone/extractor/Loader.java and src/edu/ecnu/touchstone/rule
2. experiments running is under  src/edu/ecnu/touchstone/run. Under such directory, CardinalityTest is for query cardinality experiments after the data is generated under /data/ directory. 
3. test input format should follow /test/README.md 

experiemnts to do: 
1. data fidelity 
2. data generating time 
3. scale w.r.t. data size / number of queries 


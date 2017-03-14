FTC-How to use our project
![alt tag](https://ada.csse.rose-hulman.edu/CSSE374FTC/374Project/raw/master/FTCuml.png)

1. On a windows machine run this on your command line:

java -cp designparser.jar; “full file path to bin directory of project to render” example.DesignParser {additional arguments}

2. We have provided a template for the new config file:

####################################################################################

##To store multiple values, separate by COMMA without whitespace

blacklist= (prefixes you wish to exclude, separated by commas)

whitelist= (list of fully qualified names you want to render)

recursive=(true|false) # determines whether supertypes etc. are recursively parsed

showSynthetic=(true|false) # toggles whether synthetic methods are shown in UML

SingletonDetectorColor=blue

FCOIDetectorColor=orange

AdapterDetectorColor=red

DIPViolationDetectorColor=purple

//Add additional colors/values as necessary

detectors=(List of fully qualified detector names)
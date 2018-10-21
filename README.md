Cellfie
=======

A Protégé Desktop plugin for importing spreadsheet data into OWL ontologies.

<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/cellfie/README/cellfie-screenshot.png" alt="Cellfie screenshot" width="650px"/>

Installation
------------
By default Cellfie comes together with Protégé installation bundle (starting with version 5.0.0). Any new release of the plugin will be automatically updated using Protégé auto-update mechanism. 

User's Guide
------------
To start working with Cellfie, first load your working ontology in Protégé and find the plugin in menu selection **Tools > Create axioms from Excel workbook...**

![](https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/cellfie/README/cellfie-menu-item.png)

An **Open File** window dialog will appear that request the Excel file input. After you select the file, the Cellfie window will appear showing the Excel worksheet table.

![](https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/cellfie/README/cellfie-main-window.png)

The main window consist of five major components such as:

1. **The sheet tab**: Provides you the same navigation in Excel for browsing your worksheet tables.
2. **The worksheet view**: Shows you the row and column grids where you can view the data.
3. **Transformation rule edit panel**: Provides you the functionality to add, edit, delete rules as well as to save and load your existing rules.
4. **Transformation browser**: Shows you the list of transformation rules.
5. **Generate axiom**: Triggers the creation of new axioms based on the transformation rules that map the Excel data and the ontology.

### Creating Transformation Rules

To start creating transformation rules, select the **Add** button at the transformation rule edit panel. An editor dialog will pop up where you can type the transformation expression.

<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/cellfie/README/cellfie-transformation-window.png" width="420"/>

See at the [MappingMaster wiki](https://github.com/protegeproject/mapping-master/wiki/MappingMasterDSL) for more details about the transformation expressions.

### Importing New Axioms

Once you are satisfied with all your transformation rules, continue by selecting the **Generate Axioms** button at the bottom window. Cellfie will automatically create the OWL axioms and show you the preview.

<img src="https://raw.githubusercontent.com/protegeproject/github-wiki-resources/master/cellfie/README/cellfie-generated-axioms.png" width="500">

You have two import options for these new axioms, which are, import them to a new ontology or import them to the current open ontology.

### Building and Installing

To build and install this plugin you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository
+ Apache's [Maven](http://maven.apache.org/index.html)
+ A Protégé (5.0.0 Beta 21 or higher) distribution. Download [here](http://protege.stanford.edu/products.php#desktop-protege).

Get a copy of the latest code:

    git clone https://github.com/protegeproject/cellfie-plugin.git
    
Change into the cellfie-plugin directory:

    cd cellfie-plugin

Build with Maven:

    mvn clean package  

On build completion the ```target``` directory will contain a cellfie-${version}.jar file. 
The JAR is generated in the OSGi bundle format required by Protégé's plugin-in mechanism.

To install in your local Protégé, copy this JAR file to the ```plugins``` subdirectory of your Protégé installation (e.g.,
/Applications/Protege-5.0.0/plugins/).  

### Questions

If you have questions about this plugin, please go to the main
Protégé website and subscribe to the [Protégé Developer Support
mailing list](http://protege.stanford.edu/support.php#mailingListSupport).
After subscribing, send messages to protege-dev at lists.stanford.edu.

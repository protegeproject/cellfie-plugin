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

+ [Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later
+ A tool for checking out a [Git](http://git-scm.com/) repository
+ Apache's [Maven](http://maven.apache.org/index.html)
+ A Protégé 6 distribution. Download [here](http://protege.stanford.edu/products.php#desktop-protege).

Get a copy of the latest code:

    git clone https://github.com/protegeproject/cellfie-plugin.git

Change into the cellfie-plugin directory:

    cd cellfie-plugin

Build with Maven:

    mvn clean package

On build completion the ```target``` directory will contain a cellfie-${version}.jar file.
The JAR is generated in the OSGi bundle format required by Protégé's plugin-in mechanism.

To install in your local Protégé, copy this JAR file to the ```plugins``` subdirectory of your Protégé installation (e.g.,
/Applications/Protege-6.0.0/plugins/).

### Questions

If you have questions about this plugin, please go to the main
Protégé website and subscribe to the [Protégé Developer Support
mailing list](http://protege.stanford.edu/support.php#mailingListSupport).
After subscribing, send messages to protege-dev at lists.stanford.edu.


### Protégé 6 migration

Cellfie 3 targets Protégé 6.0.0-alpha-1, OWL API 5.5.1, and Java 11.
The baseline is Cellfie 2.2.4, including its POI 5.4.0 spreadsheet support.
Cellfie 2 remains the compatible line for Protégé 5.

Build MappingMaster 3 first (`protegeproject/mapping-master`, development revision
`9cb898c2e9d574a95dce788b4edf1105cf094279`) with `mvn --batch-mode clean install`,
then run `mvn --batch-mode clean verify` here. CI pins that engine revision.
MappingMaster is embedded in the Cellfie JAR; users install only
`target/cellfie-3.0.0-SNAPSHOT.jar`, removing any older Cellfie JAR first.
Protégé and OWL API are supplied by the host, and are not bundled inside Cellfie.

The standard tag-triggered Protégé publishing workflow and `release` Maven
profile sign and publish binary, source, and Javadoc artifacts to Maven Central.
For release, publish MappingMaster 3 first, then update Cellfie's dependency to
that release before publishing Cellfie with a numeric tag matching its POM version.
Release and distribution/registry integration follow manual verification.

### Manual migration smoke test

The fixture in `src/test/resources/smoke-test` contains a small ontology,
`.xls` and `.xlsx` workbooks, and a saved mapping in `rules.json`.

1. Copy the fixture to a writable test folder and open `cellfie-smoke-test.ttl`.
2. Choose **Tools > Create axioms from Excel workbook…** and open `classes.xlsx`.
3. Choose **Load Rules** and open `rules.json`, then **Generate Axioms**.
4. Check the Apple and Banana declarations, subclass relationships to Fruit,
   and labels (including French and Chinese text). Cancel once and verify that
   generated axioms are not added. Generate again and add to the current ontology.
5. Close Cellfie; verify the class hierarchy and annotations. Undo and redo the
   import. Save to a new file and reopen it to verify the result.
6. Repeat with `classes.xls`, and exercise **Add to a new ontology**. Check the
   generated axioms and import of the source ontology. Also check an invalid
   transformation rule produces a readable error.

The migration also fixes explicit prefix lookup (e.g. `ex:Fruit`) when Protégé
renders entities by label. Prefix lookup includes imported ontology entities.

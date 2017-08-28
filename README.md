# doctemplate

This library can be used for generating documents out of templates.

## Getting Started

These instructions will get you an overview on how to implement and use the doctemplate library. See further down for
 installing or deployment notes.

### Installing

The Lib is integrated according to the desired document format:

```
docx (new microsoft word format):
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>docx-engine</artifactId>
  <version>(Newest Version)</version>
</dependency>

rtf (microsoft rich text format):
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>rtf-engine</artifactId>
  <version>(Newest Version)</version>
</dependency>

odt (open office):
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>odt-engine</artifactId>
  <version>(Newest Version)</version>
</dependency>

pdf:
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>pdf-engine</artifactId>
  <version>(Newest Version)</version>
</dependency>

xml:
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>xml-engine</artifactId>
  <version>(Newest Version)</version>
</dependency>

There's also a dependency to the commons-module:
<dependency>
  <groupId>ch.dvbern.oss.doctemplate</groupId>
  <artifactId>commons</artifactId>
  <version>(Newest Version)</version>
</dependency>
```


It might help to have a look at the specific Engine Tests. You find them here:
```
•	docx:
lib-doctemplate/docx-engine/src/test/java/ch/dvbern/lib/doctemplate/docx/DOCXMergeEngineTest.java
•	rtf:
lib-doctemplate/rtf-engine/src/test/java/ch/dvbern/lib/doctemplate/rtf/RTFMergeEngineTest.java
•	odt:
lib-doctemplate/odt-engine/src/test/java/ch/dvbern/lib/doctemplate/odt/ODTMergeEngineTest.java
•	pdf:
lib-doctemplate/pdf-engine/src/test/java/ch/dvbern/lib/doctemplate/pdf/PDFMergeEngineTest.java
•	xml:
lib-doctemplate/xml-engine/src/test/java/ch/dvbern/lib/doctemplate/xml/XmlMergeEngineTest.java
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Contributing Guidelines

Please read [Contributing.md](CONTRIBUTING.md) for the process for submitting pull requests to us.

## Code of Conduct

One healthy social atmospehere is very important to us, wherefore we rate our Code of Conduct high. For details check the file [CodeOfConduct.md](CODE_OF_CONDUCT.md)

## Authors

* **DV Bern AG** - *Initial work* - [dvbern](https://github.com/dvbern)

See also the list of [contributors](https://github.com/dvbern/doctemplate/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [License.md](LICENSE.md) file for details.


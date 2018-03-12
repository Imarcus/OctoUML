# OctoUML

![](https://media.giphy.com/media/l0HlPT6pjpTBLxBVm/giphy.gif)

OctoUML is a **UML diagram creation tool**, designed with the intention of combining formal UML notations with informal notations, specifically drawings. The user can create diagram elements either by using the formal tools or by transforming drawings to UML. 


The development of this software started of as a master's thesis. The purpose of the application is to support software designers through all phases of design. From the idea-and brainstorming-phase to the documentation-phase.

OctoUML currently only supports class diagrams. The software is designed to be interacted with using a large touch screen, it is currently being tested using a [SMART Board 800](https://education.smarttech.com/sv-se/products/smart-board-800).
The software is built using the MVC architecture in Java, utilizing JavaFX graphics library.

The library used for recognizing drawings is [Paleo Sketch](http://srl-mechanix.appspot.com/).

![](http://i68.tinypic.com/2ryt0kw.jpg "Screenshot")

## Documentation
A overview of classes can be found [here](https://ibb.co/iShvq7).

## Guides/Explanations
**Multi touch**  
When the program is first started, "Mouse activated" is not selected. To change this go to "File" and click "Mouse activated".  
When it is not selected, multiple users can create Classes, Packages and Sketches (more elements should be added) at the same time.  
When it is selected these elements can be created with the mouse and no multi touch is available.  

**Voice commands**  
To enable voice commands you first need go to VoiceController and change
```java
configuration.setGrammarPath("<location of project>")
```

## Contributors

If you want to contribute or have any questions regarding the project contact **marcus.i@live.se**

## License

GNU General Public License v3.0

Annotater
=============
https://github.com/thebrettd/annotater

A Patricia Trie is selected as the backing data structure for operations.

Trie Implememation provided by Apache Commons:
https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/trie/PatriciaTrie.html

Build/Run
=============
Spring Boot will launch an embedded tomcat server on port 8080 by default.

From Source
-------------
Source available at https://github.com/thebrettd/annotater

Navigate to the project root (where pom.xml is located), and run the maven command: 'mvn spring-boot:run'
As long as maven is installed with access to the Maven Central Repository dependencies should be downloaded automagically.

From JAR
-------------
A precompile jar is included. 

java -jar annotater-0.0.1-SNAPSHOT.jar

Dependencies
-------------
maven
Spring Boot
JSOUP
Apache Commons Collections

Methods
=============

GET "/names/{name}"
=============

Space complexity
-------------
O(C) where C is a constant. No objects are created, simply retrieve from the Trie

Runtime Complexity
-------------
O(K) time, where K is the number of bits in the largest item in the tree. 

A HashMap implementation was considered, which achieves constant time lookup, however Patricia Trie is better suited for
storing a larger number of entries. Also, by providing a prefix map of keys, we do not have to parse the entire word before
asking the map if the word should be linked. As soon as the 

PUT "/names/{name}"
=============

Space complexity
-------------
O(C) time, where C is a constant. A single new object is created and added to the Trie

Runtime complexity
-------------
O(K) time, where K is the number of bits in the largest item in the tree. A HashMap implementation was considered which
achieves constant time lookup. See GET section for trade-off analysis.

DELETE "/names"
=============

Space complexity
-------------
O(C) time, where C is a constant. No objects are created or removed from the map.

Runtime complexity
-------------
O(C) - all items are cleared from the map instantly by nulling out pointers. 

POST "/annotate"
=============

Space complexity
-------------
O(N) time, where N is the size of input. A copy of input HTML document is generated and populated, as the JSOUP framework
does not easily support destructive iteration over nodes. A visitor pattern implementation was attempted, but it was difficult
to terminate the iteration when the last element in a TextNode was also an item in the map, because a new link element would be created 
and then again visited, ad infinitum.

Runtime complexity
-------------
O(N*K) where N is the size of input and K is the number of bits in the largest item in the tree.

Other Notes/Comments
=============
* JSOUP Library seems to be changing the order of ui-sref and href attributes in link elements. 
* Also for the simple put/get operations, my implememtation does not seem to return newlines that the test script desires 
For performance reasons I will omit these, no need to send additional JSON payload ;)
* Tests are disabled - python HTML5 library output sanitizes some data as returned by Application.annotateHtml() 
e.g. double quotes in attributes
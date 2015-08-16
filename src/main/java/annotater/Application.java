package annotater;

import annotater.exception.ResourceNotFoundException;
import annotater.model.Name;
import annotater.model.URL;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class Application {

    //Map a name to its URL
    protected static final PatriciaTrie<Name> trie = new PatriciaTrie<Name>();

    private static final String notNameChar = "[^A-Za-z0-9]";


    /***
     * 2. Fetch the information for a given name using an HTTP `GET` on a URL of the form `/names/[name]`. This should return JSON in the following format:
     `{ "name": "[name goes here]", "url": "[url goes here]" }`
     * @param name
     * @return
     */
    @RequestMapping(value="/names/{name}", method= RequestMethod.GET)
    public Name getName(@PathVariable String name) {
        Name foundName = trie.get(name);
        if (foundName == null){
            throw new ResourceNotFoundException();
        }
        return foundName;
    }

    /****
     * 1. Create/update the link for a particular name using an HTTP `PUT` on a URL of the form `/names/[name]`. The body of the request contains JSON of the
     form `{ "url": "[url goes here]" }`.
     * @param name
     */
    @RequestMapping(value="/names/{name}", method= RequestMethod.PUT)
    public void putName(@PathVariable String name, @RequestBody final URL url) {
        trie.put(name, new Name(name, url.getUrl()));
    }

    /***
     * 3. Delete all the data on an HTTP `DELETE` on the URL `/names`. (Note: data is NOT required to persist between server restarts.)

     */
    @RequestMapping(value="/names", method= RequestMethod.DELETE)
    public void delete() {
        trie.clear();
    }

    /***
     *  4. On an HTTP `POST` to the URL `/annotate` where the request body is an HTML snippet, return an annotated snippet with hyperlinks on all occurrences
     of names stored in the server.
     * @param html
     * @return
     */
    @RequestMapping(value="/annotate", method= RequestMethod.POST)
    @ResponseBody
    public String annotate(@RequestBody String html) {

        return annotateHtml(html);
    }

    protected static String annotateHtml(String html) {
        Document originalDocument = Jsoup.parseBodyFragment(html);
        originalDocument.outputSettings().prettyPrint(false);

        Document annnotatedDocument = Document.createShell("");
        annnotatedDocument.outputSettings().prettyPrint(false);

        //Jsoup will wrap the HTML fragment in a body element.
        //We need to operate on all of its children.
        List<Node> nodes = originalDocument.body().childNodes();

        //Populate the response document by computing annotatedCopy of each node in the input.
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            List<Node> annotatedChildren = getAnnotatedCopy(node);
            for (int j = 0; j < annotatedChildren.size(); j++) {
                Node annotatedChild = annotatedChildren.get(j);
                annnotatedDocument.body().appendChild(annotatedChild);
            }
        }

        return annnotatedDocument.body().html();
    }

    private static List<Node> getAnnotatedCopy(Node originalNode) {
        List<Node> annotatedNodes = new ArrayList<Node>();

        if (originalNode instanceof TextNode){
            //Do not modify text which is already a link
            if (originalNode.parent() instanceof Element && ((Element) originalNode.parent()).tag().equals(Tag.valueOf("a"))){
                annotatedNodes.add(originalNode);
            }else {
                Node originalClone = originalNode.clone();
                annotatedNodes.add(originalClone);

                String wholeText = ((TextNode) originalClone).getWholeText();
                ((TextNode) originalClone).text("");

                //Starting with the first char, increment index until non-name character is found, or current word is not in trie.
                int index = 0;
                StringBuilder currWord = new StringBuilder();
                while (index < wholeText.length()) {
                    char currChar = wholeText.charAt(index);
                    //If non-name char is found, if currWord is in the dictionary annotate it, add to output.
                    if (isNotNameChar(currChar)) {
                        if (currWord.length() > 0) {
                            String finalWord = currWord.toString();
                            if (trie.containsKey(finalWord)) {
                                //Create new link element, and add it to the list of annotated nodes.
                                addAnnotatedNote(annotatedNodes, finalWord);
                                //Start a new TextNode to follow the link.
                                originalClone = addNewTextNode(annotatedNodes);
                            } else {
                                //Word not in trie, just add text to existing TextNode.
                                addUnknownNameToTextNode((TextNode) originalClone, currWord);
                            }
                        }
                        //Add delimiting char to TextNode
                        ((TextNode) originalClone).text(((TextNode) originalClone).getWholeText() + currChar);
                        currWord = new StringBuilder();
                    } else {
                        currWord.append(currChar);
                        //If currWord is not a prefix in the trie, move to first nonNameChar or end of string.
                        //Add skipped chars to existing TextNode.
                        if (trie.prefixMap(currWord.toString()).size() == 0) {
                            while (isNameChar(currChar) && index < wholeText.length() - 1) {
                                index++;
                                currChar = wholeText.charAt(index);
                                currWord.append(currChar);
                            }
                            addUnknownNameToTextNode((TextNode) originalClone, currWord);
                            currWord = new StringBuilder();
                        }
                    }
                    index += 1;
                }
                //End of string. String may have terminated without hitting nonNameChar, so check if its in the trie
                if (currWord.length() > 0) {
                    String finalWord = currWord.toString();
                    if (trie.containsKey(finalWord)) {
                        addAnnotatedNote(annotatedNodes, finalWord);
                    } else {
                        //Final name was not in trie. Just add it to existing TextNode
                        addUnknownNameToTextNode((TextNode) originalClone, currWord);
                    }
                }
            }
        }else{
            Element elementCopy = (Element) originalNode.clone();
            elementCopy.empty();

            List<Node> childNodes = originalNode.childNodes();
            for (int i = 0; i < childNodes.size(); i++) {
                Node originalChild = childNodes.get(i);
                List<Node> annotatedChildren = getAnnotatedCopy(originalChild);
                for (int j = 0; j < annotatedChildren.size(); j++) {
                    Node annotatedChild = annotatedChildren.get(j);
                    elementCopy.appendChild(annotatedChild);
                }

            }

            annotatedNodes.add(elementCopy);
        }

        return annotatedNodes;
    }

    private static TextNode addNewTextNode(List<Node> annotatedNodes) {
        TextNode newTextNode = new TextNode("", "");
        annotatedNodes.add(newTextNode);
        return newTextNode;
    }

    private static void addAnnotatedNote(List<Node> annotatedNodes, String finalWord) {
        Element linkElement = createLinkElement(finalWord);
        annotatedNodes.add(linkElement);
    }

    private static void addUnknownNameToTextNode(TextNode originalNode, StringBuilder currWord) {
        originalNode.text(originalNode.getWholeText() + currWord.toString());
    }

    private static Element createLinkElement(String finalWord) {
        Element newLink = new Element(Tag.valueOf("a"),"");
        newLink.attr("href", trie.get(finalWord).getUrl());
        newLink.text(finalWord);
        return newLink;
    }

    private static boolean isNotNameChar(char currChar) {
        return ("" + currChar).matches(notNameChar);
    }

    private static boolean isNameChar(char currChar) {
        return !("" + currChar).matches(notNameChar);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
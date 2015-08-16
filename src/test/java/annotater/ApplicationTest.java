package annotater;

import annotater.model.Name;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by brett on 8/13/15.
 */
public class ApplicationTest {

    @Before
    public void setUp(){
        Application.trie.clear();
        assertEquals(Application.trie.values().size(), 0);
    }


    @Ignore
    @Test
    public void testSimpleAnnotation() throws Exception {
        String html = "my name is alex";
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("alex", new Name("alex", "http://alex.com"));

        String document = Application.annotateHtml(html);

        assertEquals("my name is <a href=http://alex.com>alex</a>", document);
    }

    @Ignore
    @Test
    public void testMultipleNames() throws Exception {
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("alex", new Name("alex", "http://alex.com"));
        Application.trie.put("bo", new Name("bo", "http://bo.com"));
        Application.trie.put("casey", new Name("casey", "http://casey.com"));

        String html = "alex, bo, and casey went to the park.";
        String document = Application.annotateHtml(html);
        assertEquals("<a href=http://alex.com>alex</a>, <a href=http://bo.com>bo</a>, and <a href=http://casey.com>casey</a> went to the park.", document);

        html = "alex alexander alexandria alexbocasey";
        document = Application.annotateHtml(html);
        assertEquals("<a href=http://alex.com>alex</a> alexander alexandria alexbocasey", document);
    }

    @Ignore
    @Test
    public void testHTMLCorrectness() throws Exception {
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("alex", new Name("alex", "http://alex.com"));

        /***
        String html = "<div data-alex=\"alex\">alex</div>";
        String document = Application.annotateHtml(html);
        assertEquals("<div data-alex=alex><a href=http://alex.com>alex</a></div>", document);
        **/

        String html = "<a href=\"http://foo.com\">alex is already linked</a> but alex is not";
        String document = Application.annotateHtml(html);
        assertEquals("<a href=http://foo.com>alex is already linked</a> but <a href=http://alex.com>alex</a> is not", document);

        html = "<div><p>this is paragraph 1 about alex.</p><p>alex's paragraph number 2.</p><p>and some closing remarks about alex</p></div>";
        document = Application.annotateHtml(html);
        assertEquals("<div><p>this is paragraph 1 about <a href=http://alex.com>alex</a>.<p><a href=http://alex.com>alex</a>'s paragraph number 2.<p>and some closing remarks about <a href=http://alex.com>alex</a></div>", document);
    }

    @Ignore
    @Test
    public void testAdditionalAnnotations() throws Exception {
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("alex", new Name("alex", "http://alex.com"));
        Application.trie.put("bo", new Name("bo", "http://bo.com"));
        Application.trie.put("casey", new Name("casey", "http://casey.com"));

        /*
        String html = "<div data-alex=\"alex\">alex</div>";
        String document = Application.annotateHtml(html);
        assertEquals("<div data-alex=alex><a href=http://alex.com>alex</a></div>", document);
        */

        String html = "<div><p>this is paragraph 1 about alex.</p><p>alex's paragraph number 2.</p><p>and some closing remarks about alex</p></div>";
        String document = Application.annotateHtml(html);
        assertEquals("<div><p>this is paragraph 1 about <a href=http://alex.com>alex</a>.<p><a href=http://alex.com>alex</a>'s paragraph number 2.<p>and some closing remarks about <a href=http://alex.com>alex</a></div>", document);

        /*
        html = "<div><ul><li>alex</li><li>bo</li><li>bob</li><li>casey</li></ul></div><div><p>this is paragraph 1 about alex.</p><p>alex's paragraph number 2.</p><p>and some closing remarks about alex</p></div>";
        document = Application.annotateHtml(html);
        assertEquals("<div><ul><li><a href=http://alex.com>alex</a><li><a href=http://bo.com>bo</a><li>bob<li><a href=http://casey.com>casey</a></ul></div><div><p>this is paragraph 1 about <a href=http://alex.com>alex</a>.<p><a href=http://alex.com>alex</a>'s paragraph number 2.<p>and some closing remarks about <a href=http://alex.com>alex</a></div>", document);
        */

    }

    @Ignore
    @Test
    public void testAnnotationOfComplex() throws Exception {
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("Sourcegraph", new Name("Sourcegraph", "https://sourcegraph.com"));
        Application.trie.put("Milton", new Name("Milton", "https://www.google.com/search?q=milton"));
        Application.trie.put("strong", new Name("strong", "https://www.google.com/search?q=strong"));

        String html = "<div class=\\\"row\\\"><div class=\\\"col-md-6\\\"><h2> Sourcegraph makes programming <strong>delightful.</strong></h2><p>We want to make you even better at what you do best: building software to solve real problems.</p><p>Sourcegraph makes it easier to find the information you need: documentation, examples, usage statistics, answers, and more.</p><p>We're just getting started, and we'd love to hear from you. <a href=\\\"/contact\\\" ui-sref=\\\"help.contact\\\">Get in touch with us.</a></p></div><div class=\\\"col-md-4 team\\\"><h3>Team</h3><ul><li><img src=\\\"https://secure.gravatar.com/avatar/c728a3085fc16da7c594903ea8e8858f?s=64\\\" class=\\\"pull-left\\\"><div class=\\\"bio\\\"><strong>Beyang Liu</strong><br><a target=\\\"_blank\\\" href=\\\"http://github.com/beyang\\\">github.com/beyang</a><a href=\\\"mailto:beyang@sourcegraph.com\\\">beyang@sourcegraph.com</a></div></li><li><img src=\\\"https://secure.gravatar.com/avatar/d491971c742b8249341e495cf53045ea?s=64\\\" class=\\\"pull-left\\\"><div class=\\\"bio\\\"><strong>Quinn Slack</strong><br><a target=\\\"_blank\\\" href=\\\"http://github.com/sqs\\\">github.com/sqs</a><a href=\\\"mailto:sqs@sourcegraph.com\\\">sqs@sourcegraph.com</a></div></li><li><img src=\\\"https://1.gravatar.com/avatar/43ec631d6fda6a1cf42aaf875d784597?d=https%3A%2F%2Fidenticons.github.com%2F71945c68441f29a222b5689f640c956f.png&amp;r=x&amp;s=440\\\" class=\\\"pull-left\\\"><div class=\\\"bio\\\"><strong>Yin Wang</strong><br><a target=\\\"_blank\\\" href=\\\"http://github.com/yinwang0\\\">github.com/yinwang0</a><a target=\\\"_blank\\\" href=\\\"http://yinwang0.wordpress.com\\\">yinwang0.wordpress.com</a><a href=\\\"mailto:yin@sourcegraph.com\\\">yin@sourcegraph.com</a></div></li><li><img src=\\\"https://s3-us-west-2.amazonaws.com/public-dev/milton.png\\\" class=\\\"pull-left\\\"><div class=\\\"bio\\\"><strong>Milton</strong> the Australian Shepherd </div></li></ul><p><a href=\\\"/contact\\\" ui-sref=\\\"help.contact\\\">Want to join us?</a></p></div></div>";
        String document = Application.annotateHtml(html);
        assertEquals("<div class=row><div class=col-md-6><h2> <a href=https://sourcegraph.com>Sourcegraph</a> makes programming <strong>delightful.</strong></h2><p>We want to make you even better at what you do best: building software to solve real problems.<p><a href=https://sourcegraph.com>Sourcegraph</a> makes it easier to find the information you need: documentation, examples, usage statistics, answers, and more.<p>We're just getting started, and we'd love to hear from you. <a href=/contact ui-sref=help.contact>Get in touch with us.</a></div><div class=\"col-md-4 team\"><h3>Team</h3><ul><li><img src=\"https://secure.gravatar.com/avatar/c728a3085fc16da7c594903ea8e8858f?s=64\" class=pull-left><div class=bio><strong>Beyang Liu</strong><br><a target=_blank href=http://github.com/beyang>github.com/beyang</a><a href=mailto:beyang@sourcegraph.com>beyang@sourcegraph.com</a></div><li><img src=\"https://secure.gravatar.com/avatar/d491971c742b8249341e495cf53045ea?s=64\" class=pull-left><div class=bio><strong>Quinn Slack</strong><br><a target=_blank href=http://github.com/sqs>github.com/sqs</a><a href=mailto:sqs@sourcegraph.com>sqs@sourcegraph.com</a></div><li><img src=\"https://1.gravatar.com/avatar/43ec631d6fda6a1cf42aaf875d784597?d=https%3A%2F%2Fidenticons.github.com%2F71945c68441f29a222b5689f640c956f.png&amp;r=x&amp;s=440\" class=pull-left><div class=bio><strong>Yin Wang</strong><br><a target=_blank href=http://github.com/yinwang0>github.com/yinwang0</a><a target=_blank href=http://yinwang0.wordpress.com>yinwang0.wordpress.com</a><a href=mailto:yin@sourcegraph.com>yin@sourcegraph.com</a></div><li><img src=https://s3-us-west-2.amazonaws.com/public-dev/milton.png class=pull-left><div class=bio><strong><a href=\"https://www.google.com/search?q=milton\">Milton</a></strong> the Australian Shepherd </div></ul><p><a href=/contact ui-sref=help.contact>Want to join us?</a></div></div>", document);

    }

    @Ignore
    @Test
    public void testTricky() throws Exception {
        assertEquals(Application.trie.values().size(), 0);
        Application.trie.put("name", new Name("name", "https://name.com"));

        String html = "<div class='<div class=\\\"name\\\">name</a>'>name</div>";
        String document = Application.annotateHtml(html);
        assertEquals("<div class='<div class=\"name\">name</a>'><a href=https://name.com>name</a></div>", document);

    }


}
package annotater.model;

/**
 * Created by brett on 8/12/15.
 */
public class Name {

    private String name;
    private String url;

    public Name(String name, String url){
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

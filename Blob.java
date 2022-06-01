package gitlet;

import java.io.File;
import java.io.Serializable;

/** The contents of a file.
 * @author Pauline Tang
 */
public class Blob implements Serializable {

    /** Filename. */
    private String _filename;

    /** Identifier. */
    private String _identifier;

    /** Contents. */
    private String _contents;

    /** Creates a blob object with specified parameters.
     * @param filename Name of file
     * @param contents String of contents of file. */
    public Blob(String filename, String contents) {
        _filename = filename;
        _contents = contents;
        _identifier = Utils.sha1(filename, contents);
    }

    /** Serialize the Blob object. */
    public void saveBlob() {
        File f = Utils.join(Main.getBlobsFolder(), _identifier);
        Utils.writeObject(f, this);
    }

    /** Getter method for the Blob's filename.
     * @return filename. */
    public String getFilename() {
        return _filename;
    }

    /** Getter method for the Blob's filename.
     * @return contents. */
    public String getContents() {
        return _contents;
    }

    /** Getter method for the blob's identifier (Hash).
     * @return identifier. */
    public String getIdentifier() {
        return _identifier;
    }

}

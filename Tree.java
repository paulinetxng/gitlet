package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/** Tree class.
 * @author Pauline Tang*/
public class Tree implements Serializable {

    /** Keeps track of all commits made.*/
    private HashMap<String, String> commitTreeMap;

    /** Keep track of all branches made.*/
    private HashMap<String, String> _allbranches;

    /** Current head commit. */
    private Commit _headcommit;

    /** Current head branch.*/
    private String _headbranch;

    /** Creates a new Tree object. */
    public Tree() {
        commitTreeMap = new HashMap<String, String>();
        _allbranches = new HashMap<String, String>();
        _headcommit = null;
        _headbranch = "master";
    }

    /** Add a commit to the commit tree Hashmap and set the current commit
     * to the one just added.
     * @param c commit to be added. */
    public void addToTree(Commit c) {
        commitTreeMap.put(c.getIdentifier(), c.getBranch());
        _headcommit = c;
    }

    /** Return the commit tree Hashmap. */
    public HashMap<String, String> getCommitTreeMap() {
        return commitTreeMap;
    }

    /** Return the HashMap of the name of branches and its pointer to their
     * respective head commit. */
    public HashMap<String, String> getAllBranches() {
        return _allbranches;
    }

    /** Add new branch name to list of branch names. If the branch already
     * exists, change its head commit to the current head commit.
     * @param branchname name of branch.
     * @param commitid commit of id. */
    public void addBranch(String branchname, String commitid) {
        _allbranches.put(branchname, commitid);
    }

    /** Change the head branch to the given branchname.
     * @param branchname name of branch to change the head to. */
    public void changeBranch(String branchname) {
        _headbranch = branchname;
    }

    /** Getter method for the current commit.
     * @return head commit. */
    public Commit getHeadCommit() {
        return _headcommit;
    }

    /** Getter method for the current commit.
     * @return head branch. */
    public String getHeadBranch() {
        return _headbranch;
    }

    /** Serialize the tree. */
    public void saveTree() {
        File f = Utils.join(Main.getTreeFile());
        Utils.writeObject(f, this);
    }

}

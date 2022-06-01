package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/** Branch class.
 * @author Pauline Tang
 */
public class Branch implements Serializable {

    /** Name of branch. */
    private String _branchname;

    /** Head commit. */
    private String _headcommit;

    /** Path of branch. */
    private ArrayList<String> _branchpath;

    /** Creates a new Branch object.
     * @param branchname name of branch.
     * @param headcommit current head commit. */
    public Branch(String branchname, String headcommit) {
        _branchname = branchname;
        _headcommit = headcommit;
        _branchpath = new ArrayList<String>();
        changeBranchPath();
    }

    /** Return the Branch object from the hashcode of the commit.
     * @param branchname name of branch. */
    public static Branch getBranch(String branchname) {
        File f = Utils.join(Main.getBranchesFolder(), branchname);
        if (!f.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Branch br = Utils.readObject(f, Branch.class);
        return br;
    }

    /** Changes the head commit of the branch to the new given commit.
     * @param commit commit hash. */
    public void changeHead(String commit) {
        _headcommit = commit;
    }

    /** Traces the path of the branch, starting from the head commit to the
     * root. Stores the path in an ArrayList _branchpath. */
    public void changeBranchPath() {
        String currc = _headcommit;
        Commit c = Commit.getCommit(currc);

        while (c != null) {
            _branchpath.add(c.getIdentifier());
            c = c.getParent();
        }
    }

    /** Getter method for the name of this branch.
     * @return branchname. */
    public String getBranchName() {
        return _branchname;
    }

    /** Getter method for the branch's head commit id.
     * @return head commit. */
    public String getHeadCommit() {
        return _headcommit;
    }

    /** Getter method for the commit path of the branch,
     * starting from the head commit to the root.
     * @return branch path. */
    public ArrayList<String> getBranchPath() {
        return _branchpath;
    }

    /** Serialize the branch. */
    public void saveBranch() {
        File f = Utils.join(Main.getBranchesFolder(), _branchname);
        Utils.writeObject(f, this);
    }


}

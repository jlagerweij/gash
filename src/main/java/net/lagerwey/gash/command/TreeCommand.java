package net.lagerwey.gash.command;

import net.lagerwey.gash.TreeElement;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.Space;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Creates a tree of spaces and remote space uses.
 */
public class TreeCommand implements Command {

    private static final String BEGIN_ELBOW = "+";
    private static final String BEGIN_VERTICAL_AND_RIGHT = "+";
    private static final String VERTICAL = "|";
    private static final String HORIZONTAL = "-";

    @Override
    public void perform(Admin admin, String command, String arguments) {
        List<TreeElement<Space>> tree = new ArrayList<TreeElement<Space>>();
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            for (ProcessingUnitInstance processingUnitInstance : container.getProcessingUnitInstances()) {
                TreeElement<Space> treeElement = new TreeElement<Space>();
                if (processingUnitInstance.isJee()) {
                    treeElement.setKey(processingUnitInstance.getName());
                }
                for (SpaceServiceDetails spaceServiceDetails : processingUnitInstance.getSpaceDetails()) {
                    if (spaceServiceDetails.getSpaceType().equals(SpaceType.EMBEDDED)) {
                        // Embedded space
                        treeElement.setKey(spaceServiceDetails.getName());
                    } else if (spaceServiceDetails.getSpaceType().equals(SpaceType.REMOTE)) {
                        // Remote space
                        treeElement.getChildren().add(new TreeElement<Space>(spaceServiceDetails.getName()));
                    }
                }
                if (!tree.contains(treeElement)) {
                    tree.add(treeElement);
                }
            }
        }

        printElements(tree, 1, false);
    }

    private void printElements(List<TreeElement<Space>> tree, int level, boolean parentIsLastElement) {
        Collections.sort(tree, new Comparator<TreeElement<Space>>() {
            @Override
            public int compare(TreeElement<Space> o1, TreeElement<Space> o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        });
        for (int i = 0; i < tree.size(); i++) {
            TreeElement<Space> spaceTreeElement = tree.get(i);
            for (int j = 1; j < level; j++) {
                if (parentIsLastElement) {
                    Utils.print(" ");
                } else {
                    Utils.print(VERTICAL);
                }
                Utils.print("   ");
            }
            boolean isLastElement = i == tree.size() - 1;
            if (isLastElement) {
                Utils.print(BEGIN_ELBOW);
            } else {
                Utils.print(BEGIN_VERTICAL_AND_RIGHT);
            }
            Utils.println("%s%s %s", HORIZONTAL, HORIZONTAL, spaceTreeElement.getKey());
            printElements(spaceTreeElement.getChildren(), level + 1, isLastElement);
        }
    }

    @Override
    public String description() {
        return "Show a tree of spaces.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}

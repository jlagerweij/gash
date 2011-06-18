package net.lagerwey.gash.command;

import net.lagerwey.gash.TreeElement;
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

        printElements(tree, 1);
    }

    private void printElements(List<TreeElement<Space>> tree, int i) {
        Collections.sort(tree, new Comparator<TreeElement<Space>>() {
            @Override
            public int compare(TreeElement<Space> o1, TreeElement<Space> o2) {
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        });
        for (TreeElement<Space> spaceTreeElement : tree) {
            System.out.printf("%-" + (i*2) + "s%s\n", " ", spaceTreeElement.getKey());
            printElements(spaceTreeElement.getChildren(), i + 1);
        }
    }

    private TreeElement<Space> findTreeElement(List<TreeElement<Space>> tree, Space space) {
        for (TreeElement<Space> spaceTreeElement : tree) {
            if (spaceTreeElement.getKey().equals(space.getName())) {
                return spaceTreeElement;
            }
        }
        return null;
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

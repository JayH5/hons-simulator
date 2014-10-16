package za.redbridge.simulator.gp;

import org.epochx.epox.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by xenos on 10/6/14.
 */
public class EpoxRenderer {
    public static String dotRender(Node root){
        int counter = 0;
        List<String> nodeLabels = new ArrayList<>();
        List<String> connections = new ArrayList<>();
        Queue<Node> q = new ArrayDeque<>();
        q.add(root);
        while(!q.isEmpty()){
            Node cur = q.remove();
            nodeLabels.add(label(cur, counter));
            int initialQSize = q.size();
            for(int i = 0; i < cur.getArity(); i++){
                Node n = cur.getChild(i);
                connections.add(link(counter, counter+initialQSize+i+1));
                q.add(n);
            }
            counter++;
        }
        return "digraph \"GP Tree\" {"+nodeLabels.stream().reduce("", (a,b)-> a + "\n" + b) + connections.stream().reduce("", (a,b) -> a + "\n" + b) + "\n}";
    }

    protected static String label(Node n, int l){
        return "" + l + " [label=\"" + l + " " + n.getIdentifier() + "\"];";
    }

    //call this before label(); we use the counter to infer the children
    protected static String link(int from, int to){
        return from + " -> " + to + ";";
    }
}

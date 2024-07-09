
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SimpleGraphView extends View {
    private Paint paint;
    private Path path;

    private Graph graph = new Graph();

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(10);
        path = new Path();
    }

    public SimpleGraphView(Context context) {
        super(context);
        init();

    }

    public SimpleGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SimpleGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void draw(Graph graph){
        this.graph = graph;
        invalidate(); //redraw the graph
    }

    private void drawEdges(Canvas canvas, Set<Edge> edges){
        for(Edge edge: edges){
            Drawable source = edge.sourceNode.icon;
            int xSource = source.getBounds().right;
            int ySource = ((source.getBounds().bottom - source.getBounds().top) / 2) + source.getBounds().top;

            Drawable dest = edge.destNode.icon;
            int xDest = dest.getBounds().left;
            int yDest = ((dest.getBounds().bottom - dest.getBounds().top)/2) + dest.getBounds().top;

            paint.reset();
            paint.setStrokeWidth(7);
            path.reset();
            path.moveTo(xSource, ySource);
            path.lineTo(xDest, yDest);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.CYAN);
            canvas.drawPath(path, paint);

            //we want the text to appear somewhere at the center of the edge
            //that's why we need the edge length, which is the distance between
            //the two vertices of src and dest nodes
            int distance = (int) Math.sqrt(Math.pow(xSource - xDest, 2) + Math.pow(ySource - yDest, 2));
            paint.reset();
            paint.setTextSize(32);
            paint.setColor(Color.BLACK);
            canvas.drawTextOnPath(edge.upperText, path,  (int)distance/2 - 100, -25, paint);
            canvas.drawTextOnPath(edge.lowerText, path,  (int)distance/2 - 100, 45, paint);
        }
    }

    private void drawNodes(Canvas canvas, Set<Node> nodes, boolean alignLeft){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int h = metrics.heightPixels;
        int x, y,
                yCentroid, // centroid of y axis of the whole screen
                yCorrection = 170, // the adjustment that needs to be made for slight errors. This is totally random
                ySpacing = 100; // the distance that needs to be maintained between each node. This is also random
        if(alignLeft){
            x = 100;
        }else{
            x = 800;
        }
        yCentroid = h/2 - yCorrection;
        //yCentroid = h/2;
        int tempCentroid = (ySpacing * nodes.size()) / 2;
        int yOffset = yCentroid - tempCentroid;
        y = yOffset - yCorrection;

        for(Node node: nodes){
            Drawable d = node.icon;
            d.setBounds(x, y, 200+x, 200+y);
            d.draw(canvas);
            y = y + yOffset - yCorrection;

            //draw node label
            if(!node.label.isEmpty()) {
                paint.reset();
                paint.setTextSize(32);
                int textSize = node.label.length();
                int offset = 1300 / textSize; //1300 is random
                int xText = ((d.getBounds().left - d.getBounds().right) / 2) + d.getBounds().left + offset;
                int yText = d.getBounds().bottom + 30;
                paint.setColor(Color.BLACK);
                canvas.drawText(node.label, xText, yText, paint);
            }

            //draw vertex
            paint.reset();
            if(alignLeft) {
                int xVertex = d.getBounds().right;
                int yVertex = ((d.getBounds().bottom - d.getBounds().top) / 2) + d.getBounds().top;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(xVertex, yVertex, 15, paint);
            }else{
                int xVertex = d.getBounds().left;
                int yVertex = ((d.getBounds().bottom - d.getBounds().top)/2) + d.getBounds().top;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.BLUE);
                canvas.drawCircle(xVertex, yVertex, 15, paint);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Set<Node> sourceNodeList = new HashSet<>();
        Set<Node> destNodeList = new HashSet<>();
        for(Edge edge : graph.edges){
            sourceNodeList.add(edge.sourceNode);
            destNodeList.add(edge.destNode);

            //remove nodes that are already part of the source and dest list
            //this is done to figure out which nodes don't have an edge and are standalone
            graph.nodes.remove(edge.sourceNode);
            graph.nodes.remove(edge.destNode);
        }

        //for nodes without any edges, make them as Source Nodes
        //and we will display them on the left side
        sourceNodeList.addAll(graph.nodes);

        drawNodes(canvas, sourceNodeList, true);
        drawNodes(canvas, destNodeList, false);
        drawEdges(canvas, graph.edges);
    }


    public static class Node{
        private Drawable icon;
        private String label = "";
        public Node(Drawable icon){
            this.icon = icon;
        }

        public Node(Drawable icon, String label){
            this.icon = icon;
            this.label = label;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class Edge{
        private String upperText = "";
        private String lowerText = "";
        private Node sourceNode;
        private Node destNode;
        public Edge(Node sourceNode, Node destNode){
            this.sourceNode = sourceNode;
            this.destNode = destNode;
        }

        public Node getSourceNode() {
            return sourceNode;
        }

        public Node getDestNode() {
            return destNode;
        }

        public String getUpperText() {
            return upperText;
        }

        public void setUpperText(String upperText) {
            this.upperText = upperText;
        }

        public String getLowerText() {
            return lowerText;
        }

        public void setLowerText(String lowerText) {
            this.lowerText = lowerText;
        }
    }

    public static class Graph{
        private Set<Node> nodes;
        private Set<Edge> edges;

        public Graph(){
            this.nodes = new HashSet<>();
            this.edges = new HashSet<>();
        }

        public Graph(List<Node> nodeList, List<Edge> edgeList){
            this.nodes = new HashSet<>(nodeList);
            this.edges = new HashSet<>(edgeList);
        }

        public Set<Node> getNodes() {
            return nodes;
        }

        public Set<Edge> getEdges() {
            return edges;
        }

        public void addAllNodes(List<Node> nodeList){
            this.nodes.addAll(nodeList);
        }

        public void addAllEdges(List<Edge> edgeList){
            this.edges.addAll(edgeList);
        }
    }
}
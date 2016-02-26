//package commands;
//
//import flexisketch.*;
//
///**
// * Created by Marcus on 2016-01-29.
// */
//public class AddDeleteDrawingCommand implements Command {
//
//    private Symbol aSymbol;
//   // private GraphPanel aPanel;
//    private boolean aAdding;
//
//    public AddDeleteDrawingCommand(/*GraphPanel pPanel*/ Symbol pSymbol, boolean pAdding){
//        aSymbol = pSymbol;
//        //aPanel = pPanel;
//        aAdding = pAdding;
//    }
//
//    /**
//     * Undoes the command and adds/deletes the node.
//     */
//    @Override
//    public void undo() {
//        if(aAdding)
//        {
//            delete();
//        }
//        else
//        {
//            add();
//        }
//    }
//
//    /**
//     * Performs the command and adds/deletes the symbol.
//     */
//    public void execute()
//    {
//        if(aAdding)
//        {
//            add();
//        }
//        else
//        {
//            delete();
//        }
//    }
//
//    /**
//     * Removes the symbol from the graph.
//     */
//    /*private void delete()
//    {
//        aPanel.removeSymbol(aSymbol);
//    }*/
//
//    /**
//     * Adds the symbol to the graph at the point in its properties.
//     */
//    /*private void add()
//    {
//        aPanel.addSymbol(aSymbol);
//    }*/
//}

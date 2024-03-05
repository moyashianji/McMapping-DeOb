package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List; // List のインポートを追加

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Main extends JFrame {

    private JTable table;
    private JTable centertable;

    private JTextField searchText;
    private JTextArea resultArea;
    private JTextArea textArea;

    public Main() {
        super("\u96e3\u8aad\u5316\u89e3\u9664\u304f\u3093");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);

        JPanel mainPanel = new JPanel(new GridLayout(1, 3)); // GridLayoutに変更
        add(mainPanel, BorderLayout.CENTER);

        /**
         * 一個目のCSVのロード
         */
        JPanel leftPanel = new JPanel(new BorderLayout());
        mainPanel.add(leftPanel);

        JButton browseButton = new JButton("method.csv\u30ed\u30fc\u30c9");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("CSV\u30d5\u30a1\u30a4\u30eb\u3092\u9078\u3093\u3067\u304f\u3060\u3055\u3044");
                fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

                int userSelection = fileChooser.showOpenDialog(Main.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    displayCSV(selectedFile);
                }
            }
        });
        leftPanel.add(browseButton, BorderLayout.NORTH);

        table = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        /**
         * 二個目のCSVのロード
         */
        JPanel centerPanel  = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel);

        JButton centerbrowseButton = new JButton("field.csv\u30ed\u30fc\u30c9");
        centerbrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("CSV\u30d5\u30a1\u30a4\u30eb\u3092\u9078\u3093\u3067\u304f\u3060\u3055\u3044");
                fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

                int userSelection = fileChooser.showOpenDialog(Main.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    display2CSV(selectedFile);
                }
            }
        });
        centerPanel.add(centerbrowseButton, BorderLayout.NORTH);

        centertable = new JTable();
        JScrollPane centertableScrollPane = new JScrollPane(centertable);
        centerPanel.add(centertableScrollPane, BorderLayout.CENTER);

        /**
         * テキストファイルのロード
         */
        JPanel rightPanel = new JPanel(new BorderLayout());
        mainPanel.add(rightPanel);

        JButton browseTextButton = new JButton("\u5909\u63db\u3057\u305f\u3044\u30d5\u30a1\u30a4\u30eb\u9078\u629e(TXT)");
        browseTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("\u30c6\u30ad\u30b9\u30c8\u30d5\u30a1\u30a4\u30eb\u9078\u3093\u3067\u304f\u3060\u3055\u3044");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

                int userSelection = fileChooser.showOpenDialog(Main.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    highlightMatches(selectedFile);
                    replaceMatches(selectedFile);
                }
            }
        });
        rightPanel.add(browseTextButton, BorderLayout.NORTH);

        resultArea = new JTextArea();
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        rightPanel.add(resultScrollPane, BorderLayout.CENTER);

        /**
         * DD
         */
        resultArea.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = evt.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : droppedFiles) {
                            if (file.isFile()) {
                                if (file.getName().endsWith(".txt")) {
                                    replaceMatches(file); // テキストファイルを表示するメソッドを呼び出す
                                    highlightMatches(file);

                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        setVisible(true);
    }
    private void replaceMatches(File textFile) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        DefaultTableModel centertableModel = (DefaultTableModel) centertable.getModel();

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // テーブルの一列目の文字列と一致する部分をテーブルの該当する行の二列目の文字列で置換
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String cellValue = (String) tableModel.getValueAt(i, 0);
                    String replacementValue = (String) tableModel.getValueAt(i, 1);
                    line = line.replace(cellValue, replacementValue);
                }

                for (int i = 0; i < centertableModel.getRowCount(); i++) {
                    String cellValue = (String) centertableModel.getValueAt(i, 0);
                    String replacementValue = (String) centertableModel.getValueAt(i, 1);
                    line = line.replace(cellValue, replacementValue);
                }

                content.append(line).append("\n");
            }
            resultArea.setText(content.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void highlightMatches(File textFile) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        DefaultTableModel centertableModel = (DefaultTableModel) centertable.getModel();

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // テーブルの一列目の文字列と一致する部分を赤くする
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String cellValue = (String) tableModel.getValueAt(i, 0);
                    if (line.contains(cellValue)) {
                        int startIndex = line.indexOf(cellValue);
                        int endIndex = startIndex + cellValue.length();
                        line = line.substring(0, startIndex) + "<font style='background-color:red;'>" +
                                line.substring(startIndex, endIndex) + "</font>" + line.substring(endIndex);
                    }
                }
                // テーブルの一列目の文字列と一致する部分を青くする
                for (int i = 0; i < centertableModel.getRowCount(); i++) {
                    String cellValue = (String) centertableModel.getValueAt(i, 0);
                    if (line.contains(cellValue)) {
                        int startIndex = line.indexOf(cellValue);
                        int endIndex = startIndex + cellValue.length();
                        line = line.substring(0, startIndex) + "<font style='background-color:blue;'>" +
                                line.substring(startIndex, endIndex) + "</font>" + line.substring(endIndex);
                    }
                }
                content.append(line).append("<br>"); // 改行を追加
            }

            // HTMLコンテンツをJTextPaneにセット
            JTextPane resultPane = new JTextPane();
            resultPane.setContentType("text/html");
            resultPane.setText("<html><body>" + content.toString() + "</body></html>");

            // スクロール可能な領域に表示
            JScrollPane resultScrollPane = new JScrollPane(resultPane);
            resultScrollPane.setPreferredSize(new Dimension(400, 300));

            // 結果を表示
            JFrame frame = new JFrame("\u5909\u63db\u6587\u5b57\u5217\u3092\u8272\u4ed8\u3051\uff08\u4e00\u90e8\u305f\u3076\u3093\u30d0\u30b0\u3063\u3066\u308b\uff09");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(resultScrollPane);
            frame.pack();
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayCSV(File file) {
        try (FileReader reader = new FileReader(file);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            DefaultTableModel tableModel = new DefaultTableModel();

            // ヘッダーの設定
            for (String header : csvParser.getHeaderNames()) {
                tableModel.addColumn(header);
            }

            // データの設定
            for (CSVRecord record : csvParser) {
                Object[] rowData = new Object[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    rowData[i] = record.get(i);
                }
                tableModel.addRow(rowData);
            }

            table.setModel(tableModel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void display2CSV(File file) {
        try (FileReader reader = new FileReader(file);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            DefaultTableModel tableModel = new DefaultTableModel();

            // ヘッダーの設定
            for (String header : csvParser.getHeaderNames()) {
                tableModel.addColumn(header);
            }

            // データの設定
            for (CSVRecord record : csvParser) {
                Object[] rowData = new Object[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    rowData[i] = record.get(i);
                }
                tableModel.addRow(rowData);
            }

            centertable.setModel(tableModel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}
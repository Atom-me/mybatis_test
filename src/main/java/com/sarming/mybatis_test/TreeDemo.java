package com.sarming.mybatis_test;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class Node {
    private Integer id;
    private Integer pid;
    private String name;


    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", pid=" + pid +
                ", name='" + name + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

/**
 * CREATE TABLE `tree` (
 *   `id` int(11) NOT NULL AUTO_INCREMENT,
 *   `pid` int(11) NOT NULL,
 *   `name` varchar(50) NOT NULL,
 *   PRIMARY KEY (`id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4
 *
 * INSERT INTO mydb.tree (id, pid, name) VALUES (1, -1, 'china');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (2, 1, 'beijing');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (3, 1, 'tianjin');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (4, 2, 'miyun');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (5, 4, 'miyunxian');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (6, 3, 'jinghai');
 * INSERT INTO mydb.tree (id, pid, name) VALUES (7, 3, 'hexi');
 *
 *
 * @author Atom
 */
public class TreeDemo {


    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=true", "root", "root");
        return connection;
    }

    public static Node getRootNode() throws Exception {
        Node rootNode = null;
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        ResultSet executeQuery = statement.executeQuery("select * from tree where pid = -1");
        //get root
        while (executeQuery.next()) {
            rootNode = new Node();
            int id = executeQuery.getInt(1);
            int pid = executeQuery.getInt(2);
            String name = executeQuery.getString(3);
            rootNode.setId(id);
            rootNode.setPid(pid);
            rootNode.setName(name);
        }

        return rootNode;
    }


    public static List<Node> getChildrenNode(Node node) throws Exception {
        List<Node> children = new ArrayList<>();
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        ResultSet executeQuery = statement.executeQuery("select * from tree where pid = " + node.getId());
        //get children
        while (executeQuery.next()) {
            Node rootNode = new Node();
            int id = executeQuery.getInt(1);
            int pid = executeQuery.getInt(2);
            String name = executeQuery.getString(3);
            rootNode.setId(id);
            rootNode.setPid(pid);
            rootNode.setName(name);
            children.add(rootNode);
        }
        return children;

    }

    public static void recursionCreate(Node node, String path) throws Exception {
        //终止条件？
        List<Node> childrenNode = getChildrenNode(node);
        if (childrenNode.isEmpty()) {
            return;
        }

        for (Node child : childrenNode) {
            File childDir = new File(path + File.separator + child.getName());
            if (!childDir.exists()) {
                childDir.mkdirs();
                System.out.println("create dir :::" + childDir.getCanonicalPath());
            }
            recursionCreate(child, childDir.getCanonicalPath());
        }

    }

    public static void main(String[] args) throws Exception {
        Node rootNode = getRootNode();
        System.err.println("rootNode:::" + rootNode);
        //create root dir
        File rootDir = new File("/Users/atom/testDir" + File.separator + rootNode.getName());
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        //create sub dir
        List<Node> nodeList = getChildrenNode(rootNode);

        //这种做法不可以
       /* while (!nodeList.isEmpty()) {
            for (Node node : nodeList) {
                System.err.println(node);
                File childDir = new File(rootDir.getCanonicalPath() + File.separator + node.getName());
                if (!childDir.exists()) {
//                    childDir.mkdirs();
                    System.out.println("create dir :::"+childDir.getCanonicalPath());
                }
                nodeList = getChildrenNode(node);
            }
        }*/


        for (Node node : nodeList) {
            File nodeDir = new File(rootDir.getCanonicalPath() + File.separator + node.getName());
            if (!nodeDir.exists()) {
                nodeDir.mkdirs();
                System.out.println("create dir :::" + nodeDir.getCanonicalPath());

            }
            recursionCreate(node, nodeDir.getCanonicalPath());
        }

    }
}

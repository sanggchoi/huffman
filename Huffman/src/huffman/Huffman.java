package huffman;
import java.io.*;   
import java.nio.file.*;
import java.util.*;
/**
 * @author sanggon.choi
 */
public class Huffman {
    private int[] freqs_;
    private List<Node> bytes_;
    private Node root_;
    private byte[] chars_;
    private String decode_;
    public Huffman(String path, String filename){
        freqs_ = new int[256];
        bytes_ = new ArrayList();
        root_ = new Node();
        initialize(path+filename);
        buildTree();
        encodeTo(new File(path+"compressed.txt"));
        decode(path+"compressed.txt",path+"decompressed.txt");
    }
    
    public void initialize(String path){
        try{
            chars_ = Files.readAllBytes(Paths.get(path));
            for(int n = 0;n < chars_.length;n++){
                freqs_[chars_[n]+128]++;
            }
        }catch(IOException e){
            System.out.println(e);
        }
        for(int n = 0;n < freqs_.length;n++){
            if(freqs_[n] > 0){
                Node node = new Node();
                node.setByte((byte)(n-128),freqs_[n]);
                bytes_.add(node);
            }
        }
    } 
    
    public void buildTree(){
        if(bytes_.size() >= 2){
            List<Node> list = bytes_;
            while(list.size() > 1){
                Node n = new Node();
                n.setLeft(list.get(1));
                n.setRight(list.get(0));
                n.setValue();
                list.remove(0);
                list.remove(0);
                list.add(n);
                Collections.sort(list);
            }
            root_ = list.get(0);
        }else if(bytes_.size() == 1){
            root_.setLeft(bytes_.get(0));
            root_.setValue();
        }
    }
    public void encodeTo(File file){
        Map<Byte,String> map = new HashMap();
        String s = new String();
        buildBinaryMap(root_,s,map);
        for(int n = 0;n < chars_.length;n++){
            s += map.get(new Byte(chars_[n]));
        }
        String encoded = padBinary(buildBinaryTree(root_)+buildBytes(root_)+s);
        try{
            FileOutputStream fo = new FileOutputStream(file);
            byte[] b = binaryToByteArray(encoded);
            fo.write(b);
            fo.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }
    
    public String padBinary(String s){
        String pad = new String();
        int length = s.length();
        for(int n = 1;n < 8-(length%8);n++){
            pad += "0";
        }
        pad += "1";
        return pad+s;
    }
    
    public byte[] binaryToByteArray(String s){
        byte[] b = new byte[s.length()/8];
        for(int n = 0;n < b.length;n++){
            b[n] = (byte)Integer.parseInt(s.substring(n*8,(n*8)+8),2);
        }
        return b;
    }
    
    public void buildBinaryMap(Node n,String binary,Map map){
        if(n.left_ != null || n.right_ != null){
            if(n.left_ != null){
                buildBinaryMap(n.left_,binary+"0",map);
            }
            if(n.right_ != null){
                buildBinaryMap(n.right_,binary+"1",map);
            }
        }else{
            map.put(n.byte_,binary);
        }
    }
    
    public String buildBinaryTree(Node n){      
        String s = new String();
        if(n.left_ != null){
            s += "1"+buildBinaryTree(n.left_);
        }else{
            s += "0";
        }
        if(n.right_ != null){
            s += "1"+buildBinaryTree(n.right_);
        }else{
            s += "0";
        }
        return s;
    }
    
    public String buildBytes(Node n){
        if(n.left_ != null || n.right_ != null){  
            String s = new String();
            if(n.left_ != null){
                s += buildBytes(n.left_);
            }
            if(n.right_ != null){
                s += buildBytes(n.right_);
            }
            return s;
        }
        String s = Integer.toString((int)n.byte_+128,2);
        String s2 = new String();
        int length = s.length();
        if(length < 8){
            for(int i = 0;i < 8-(length%8);i++){        
                s2 += "0";
            }
        }
        return s2+s;
    }
    
    public void decode(String encodedPath,String decodePath){
        Map<String,Byte> map = new HashMap();
        try{
            byte[] bytes = Files.readAllBytes(Paths.get(encodedPath));
            decode_ = byteToBinary(bytes);
            decode_ = decode_.substring(decode_.indexOf("1")+1);
            Node n = readTree();
            buildDecodeMap(n,new String(),map);
        }catch(IOException e){
            System.out.println(e);
        }
        int msgLength = decode_.length();
        List<Byte> bList = new ArrayList();
        try{
            FileOutputStream fo = new FileOutputStream(new File(decodePath));
            int i = 0;
            for(int n = 0;n < msgLength;n++){
                if(map.containsKey(decode_.substring(i,n))){
                    bList.add(map.get(decode_.substring(i,n)).byteValue());
                    i = n;
                }
            }
            byte[] b = new byte[bList.size()];
            for(int n = 0;n < bList.size();n++){
                b[n] = bList.get(n);
            }
            fo.write(b);
            fo.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }
    
    public String byteToBinary(byte[] b){
        String s = new String();
        for(int n = 0;n < b.length;n++){
            s += String.format("%8s",Integer.toBinaryString(b[n]&0xff)).replace(' ', '0');
        }
        return s;
    }
    
    public Node readTree(){
        Node root = new Node();
        if(decode_.charAt(0) == '1'){ 
            decode_ = decode_.substring(1);
            root.left_ = readTree();
        }else{
            decode_ = decode_.substring(1);
        }
        if(decode_.charAt(0) == '1'){
            decode_ = decode_.substring(1);
            root.right_ = readTree();
        }else{
            decode_ = decode_.substring(1);
        }
        return root;
    }
    
    public void buildDecodeMap(Node n,String binary,Map map){
        if(n.left_ != null || n.right_ != null){
            if(n.left_ != null){
                buildDecodeMap(n.left_,binary+"0",map);
            }
            if(n.right_ != null){
                buildDecodeMap(n.right_,binary+"1",map);
            }
        }else{
            int i = Integer.valueOf(decode_.substring(0,8),2);
            byte b = Byte.parseByte(Integer.toString(i-128));
            decode_ = decode_.substring(8);
            map.put(binary,b);
        }
    }
    
    public void treePrint(Node n){
        helper(n);
    }
    
    public void helper(Node n){
        printChildren(n);
        if(n.left_ != null){
            helper(n.left_);
        }
        if(n.right_ != null){
            helper(n.right_);
        }
    }
    
    public void printChildren(Node n){
        System.out.print(n.value_+" ");
        if(n.left_ != null){
            System.out.print(n.left_.value_+" ");
        }
        if(n.right_ != null){
            System.out.print(n.right_.value_+" ");
        }
        System.out.println();
    }
    
    private class Node implements Comparable<Node>{
        private Node left_;
        private Node right_;
        private byte byte_;
        private int value_;
        private void setLeft(Node left){
            left_ = left;
        }
        
        private void setRight(Node right){
            right_ = right;
        }
        
        private void setByte(byte bytes,int frequency){
            byte_ = bytes;
            value_ = frequency;
        }
        
        private void setValue(){
            value_ = 0;
            if(left_ != null){
                value_ += left_.value_;
            }
            if(right_ != null){
                value_ += right_.value_;
            }
        }
        
        public int compareTo(Node compare){
            return value_-compare.value_;
        }
    }
}

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class VectorQuantizaion extends javax.swing.JFrame {

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    
   
    
     public static int[][] ReadImage(String filePath)
    {
	    int width=0;
		int height=0;
        File file=new File(filePath);
        BufferedImage image=null;
        try
        {
            image=ImageIO.read(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

          width=image.getWidth();
          height=image.getHeight();
        int[][] pixels=new int[height][width];

        for(int x=0;x<width;x++)
        {
            for(int y=0;y<height;y++)
            {
                int RGB=image.getRGB(x, y);
                int alpha=(RGB >> 24) & 0xff;
                int RED = (RGB >> 16) & 0xff;
                int GREEN = (RGB >> 8) & 0xff;
                int BLUE = (RGB >> 0) & 0xff;

                pixels[y][x]=BLUE;
            }
        }

        return pixels;
    }
    
    public static void writeImage(int[][] pixels,String outputFilePath,int width,int height)
    {
        File fileout=new File(outputFilePath);
        BufferedImage image2=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB );

        for(int x=0;x<width ;x++)
        {
            for(int y=0;y<height;y++)
            {
                image2.setRGB(x,y,(pixels[y][x]<<16)|(pixels[y][x]<<8)|(pixels[y][x]));
            }
        }
        try
        {
            ImageIO.write(image2, "jpg", fileout);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
  
    void ShowVector ( vector v)
    {
        for (int i=0 ; i<v.height ; i++ )
        {
            for (int j=0 ; j<v.width ; j++)
            {
                System.out.print(v.data[i][j] + "  ");
            }
            System.out.println();
        }
        
        System.out.println("---------------------------");
    }
    
    ArrayList <vector> Build_vectors (int [][] originalImage , vector [][] vectors , int numOfRows , int numOfCols , int widthOfBlock , int heightOfBlock)
    {   
        
        ArrayList <vector> AllVectors = new ArrayList<>();
        vector curVector = new vector ( widthOfBlock , heightOfBlock );
        
        for (int i=0 ; i<originalImage.length ; i+=heightOfBlock)
        {
            for (int j=0 ; j<originalImage[0].length ; j+=widthOfBlock)
            {   
               int x = i ;
               int z = j ;
               curVector = new vector ( widthOfBlock , heightOfBlock );
               
               for (int n=0 ; n<heightOfBlock ; n++)
                {
                    for (int m=0 ; m<widthOfBlock ; m++)
                    {    
                        curVector.data[n][m]= originalImage[x][z];
                    }
                    
                    x++;
                    z=j;
                }  

                AllVectors.add(curVector);
            }
        }
        
        int indx =0 ;
        
        for (int i=0 ; i<numOfRows ; i++) 
        {
            for (int j=0 ; j<numOfCols ; j++)
            {
                vectors[i][j] = AllVectors.get(indx++);
            }
        }
        
        return AllVectors ;
   }
    
    int indxOF_min_distance (ArrayList <Double> distance_difference )
    {
        double min_diff = distance_difference.get(0); // assume first element is the min 
        int indx = 0 ;
        
        for (int i=1 ; i<distance_difference.size() ; i++)
        {
            if ( distance_difference.get(i) < min_diff)
            {
               min_diff = distance_difference.get(i);
               indx = i ;
            }
            
        }
        
        return indx ;
    }
    
     ArrayList<vector> associate ( ArrayList<vector> split , ArrayList <vector> data  ) // associate ang return avg
    {   
        
        ArrayList <split_element> Split = new ArrayList<>();
        ArrayList <vector> Averages = new ArrayList<> ();
        int width = data.get(0).width;
        int height = data.get(0).height ;
                
        for (int i = 0; i < split.size(); i++)  // inilialization 
        {  
           split_element initial = new split_element() ;
           initial.setValue(split.get(i));
           Split.add(initial);
        }
        
        for (int i=0 ; i<data.size() ; i++) // associate data
        {
                  vector cur = data.get(i);
                  ArrayList <Double> distance_difference = new ArrayList<> ();
               
                  
                  for (int j=0 ; j<split.size() ;j++)
                  {   
                      double total_diff = 0 ;
                      
                      for (int w=0 ; w<width ; w++)
                      {
                          for (int h=0 ; h<height ; h++)
                          {
                              double value = cur.data[w][h]-split.get(j).data[w][h];
                              double distanc_diff =  Math.pow( value , 2);
                              total_diff +=distanc_diff ;
                          }
                      }
                   
                    distance_difference.add(total_diff);
                      
                 }
                  
                  int indx = indxOF_min_distance (distance_difference);
                  
                  ArrayList <vector> cur_associated = Split.get(indx).getAssoicated();
                  
                  cur_associated.add(cur);
                 
                  split_element New = new split_element(Split.get(indx).getValue() , cur_associated);
                  
                  Split.set(indx , New );
                  
          }

        for (int i=0 ; i<Split.size() ; i++) 
        {
            int arraysize = Split.get(i).getAssoicated().size();
            vector avg = new vector(width , height);
            
            for (int w = 0; w < width; w++) 
            {
                for (int h = 0; h < height; h++) 
                {   
                    double total = 0 ;
                   
                    for (int j = 0; j < arraysize; j++) 
                    {   
                        total+= Split.get(i).getAssoicated().get(j).data[w][h];
                    }
                    
                    avg.data[w][h]= total/arraysize;
                } 
               
            }
              
                Averages.add(avg);
            
        }
        
        return Averages ;
    }
    
     ArrayList<vector> Split (ArrayList <vector> Averages ,  ArrayList <vector> data , int numoflevels ) // split original averages
    {
         int width = Averages.get(0).width ;
         int height = Averages.get(0).height ;
       
         for (int i=0 ; i<Averages.size() ; i++)
        {   
            if (Averages.size()<numoflevels)
            {
              
            ArrayList <vector> split = new ArrayList<>();
            
            for (int j=0 ; j<Averages.size() ; j++)
            {   
              vector left = new vector( width , height);
              vector right = new vector( width , height);
              
               for (int w=0 ; w<width ; w++)
               {
                   for (int h=0 ; h<height ; h++)
                   {   
                       int cast = (int)Averages.get(j).data[w][h] ;
                       
                       left.data[w][h]= cast;
                       right.data[w][h]= cast+1;
                   
                   }
              
               }
              
              split.add(left);
              split.add(right);  
            }
            
            Averages.clear();
           
            Averages = associate( split , data);
            
            i=0 ;
            
            }
            
            else 
                break;
            
        } 
         
         return Averages ;
    }
    
     ArrayList<vector> modify ( ArrayList<vector> prev_Averages , ArrayList<vector> new_Averages , ArrayList<vector> data  )
    {
       while (true)
        { 
           int width = new_Averages.get(0).width;
           int height = new_Averages.get(0).height;
           int totaldiff = 0 ;
           int avgdiff = 0 ;
           
           for (int i=0 ; i<new_Averages.size() ; i++)
           {   
               double DiffOf2vec =0 ; 
                       
               for (int w=0 ; w<width ; w++)
               {
                   for (int h=0 ; h<height ; h++)
                   {
                       DiffOf2vec += Math.abs(prev_Averages.get(i).data[w][h] - new_Averages.get(i).data[w][h]) ;
                   }
               }
              
              totaldiff+=DiffOf2vec;
           }
           
           avgdiff = totaldiff / prev_Averages.size() ;
           
           if (avgdiff < 0.0001 )
           {
               break;
           }
           
           else 
           {
               prev_Averages = new_Averages ;
               new_Averages = associate( new_Averages , data);
           }
           
        }
       
       return new_Averages ;
        
    }
     
    void Quantization ( int numoflevels ,  ArrayList <vector> data , int widthOfBlock , int heightOfBlock , vector [][] vectors , int numOfRows , int numOfCols  )
    {    
         ArrayList <vector> Averages = new ArrayList<>();         
        vector first_avg = new vector( widthOfBlock , heightOfBlock );
        
        for (int w = 0; w < widthOfBlock; w++) 
        {  
            for (int h = 0; h < heightOfBlock; h++) 
            {   
                 double total = 0 ;
                
                for (int i = 0; i < data.size(); i++) 
                {
                        total += data.get(i).data[w][h];
                    
                }
                
                first_avg.data[w][h] = total/data.size();

           }

        }

        Averages.add(first_avg);
      
        Averages = Split (Averages , data , numoflevels );
        System.out.println("Splited");
        
        System.out.println("Averages: ");
        for (int i=0 ; i<Averages.size()  ; i++)
        {
            ShowVector(Averages.get(i));
        }
        
        ArrayList<vector> prev_Averages = Averages ;
        ArrayList<vector> new_Averages = associate( Averages , data); 
        
        new_Averages = modify(prev_Averages, new_Averages, data);
       
        System.out.println("--------------- New Average ---------------------");
       
        for (int x=0 ; x<new_Averages.size() ; x++)
        {
            ShowVector(new_Averages.get(x));
        }
        
        ArrayList <vector> codeBook = new ArrayList<>();
        
        for (int i=0 ; i<new_Averages.size() ; i++)
        {
            codeBook.add(new_Averages.get(i));
        }
        
        
        int indx =0 ;
        
        
        for (int i=0 ; i<widthOfBlock ; i++) 
        {
            for (int j=0 ; j<numOfCols ; j++)
            {
                vectors[i][j] = data.get(indx++);
            }
        }
        
        compress (codeBook , vectors );
 
    } 
    
    void compress ( ArrayList<vector> codeBook , vector [][] vectors )
    {
       int Rows = vectors.length ;
       int Cols = vectors[0].length ;
       int [][] comp_image = new int [Rows][Cols];
       
       for (int i=0 ; i<Rows ; i++)
       {
           for (int j=0 ; j<Cols ; j++)
           {
                vector cur = vectors[i][j];
                ArrayList <Double> distance_difference = new ArrayList<> ();
                
                for (int k=0 ; k<codeBook.size() ;k++)
                {   
                    double total_diff = 0 ;
                  
                    for (int w=0 ; w<codeBook.get(0).width ; w++)
                    {
                        for (int h = 0; h < codeBook.get(0).height; h++)
                        {
                            double value = cur.data[w][h] - codeBook.get(k).data[w][h];
                            double distanc_diff = Math.pow(value, 2);
                            total_diff += distanc_diff;
                        }
                    }
                    
                    distance_difference.add(total_diff);
                }
                
                int indx = indxOF_min_distance (distance_difference);
                comp_image[i][j]= indx ;
               
           }
       }
      
        Save_CodeBook_CompImg ( codeBook , comp_image);
       
    }
    
    Scanner sc;

    public void open_file(String FileName) {
        try {
            sc = new Scanner(new File(FileName));
        } catch (Exception e) {

        }
    }

    public void close_file() {
        sc.close();
    }

    Formatter out; 

    public void openfile(String pass) {
        try {
            out = new Formatter(pass);
        } catch (Exception e) {
        }

    }

    public void closefile() {
        out.close();
    }
    
    void write(String code) {

        out.format("%s", code);
        out.format("%n");
        out.flush();

    }
    
    
    void Decompress ()
    {
        
        ArrayList<vector> codeBook = new ArrayList <vector>();
        int [][] comp_image = new int [1][1] ; 
        comp_image = Reconstruct( codeBook , comp_image);
        int [][] Decomp_image = new int [originalImage.length][originalImage[0].length];  
        
        for (int i=0 ; i<comp_image.length ; i++)
        {
            for (int j=0 ; j<comp_image[0].length ; j++)
            {
                vector cur = new vector();
                cur = codeBook.get(comp_image[i][j]);
                
                int cornerx = i*cur.height;
                int cornery = j*cur.width ;
                
                
                for (int h=0 ; h<cur.height ; h++)
                {
                    
                    for (int k=0 ; k<cur.width ; k++)
                    {
                        Decomp_image[cornerx+h][cornery+k] = (int) cur.data[h][k];
                    }
                }
                
            }
        }
        
        
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println(Decomp_image);
        writeImage(Decomp_image, "C:\\Users\\alaaf\\eclipse-workspace\\LastVersionOfVectorQuantization/Decompress.jpg", Decomp_image[0].length, Decomp_image.length);
     
    }
    
    void Save_CodeBook_CompImg ( ArrayList<vector> codeBook , int [][] comp_image )
    {
        openfile("C:\\Users\\alaaf\\eclipse-workspace\\LastVersionOfVectorQuantization/CompressFile.txt");
        String codeBookSize = "" + codeBook.size();
        String WidthOfBlock = "" + codeBook.get(0).width;
        String heightOfBlock = "" + codeBook.get(0).height;
        
        write(codeBookSize);
        write(WidthOfBlock);
        write(heightOfBlock);
        
        for (int i=0 ; i<codeBook.size() ; i++)
        {
            for (int w=0 ; w<codeBook.get(i).width ; w++)
            { 
                String row = "";
                
                for (int h=0 ; h<codeBook.get(i).height ; h++)
                {
                    row += codeBook.get(i).data[w][h] + " ";
                }
                
                write(row);
            }
            
        } 
        String com_image_height = "" + comp_image.length ;
        write(com_image_height);
        String com_image_width = "" + comp_image[0].length ;
        write(com_image_width);
        
        for (int i=0 ; i<comp_image.length ; i++)
        {   
            String row = "";
            
            for (int j=0 ; j<comp_image[0].length ; j++)
            {
                row+= comp_image[i][j] +" ";
            }
            
            write(row);
        }
        
        closefile();
    }
    
    
    int [][] Reconstruct( ArrayList<vector> codeBook , int [][] comp_image)
    {
        open_file("C:\\Users\\alaaf\\eclipse-workspace\\LastVersionOfVectorQuantization/CompressFile.txt");
        int codeBookSize = Integer.parseInt(sc.nextLine());
        int WidthOfBlock = Integer.parseInt(sc.nextLine());
        int heightOfBlock = Integer.parseInt(sc.nextLine());
        
        for (int i=0 ; i<codeBookSize ; i++)
        {
            vector cur = new vector(WidthOfBlock , heightOfBlock);
             
            for (int w=0 ; w<WidthOfBlock ; w++)
            {  
                String row = sc.nextLine();
                String [] elements = row.split(" ");
                
                for (int h=0 ; h<heightOfBlock ; h++)
                {
                   
                  cur.data[w][h]= Double.parseDouble(elements[h]);
                 
                }
                
            }
            
            codeBook.add(cur);
           
        }  
        
        int com_image_height = Integer.parseInt(sc.nextLine());
        int com_image_width =  Integer.parseInt(sc.nextLine());
        comp_image = new int [com_image_height][com_image_width];
        
        for (int i=0 ; i<comp_image.length ; i++)
        {   
            String line = sc.nextLine();
            String [] row = line.split(" ");
            
            for (int j=0 ; j<comp_image[0].length ; j++)
            {
                comp_image[i][j] = Integer.parseInt(row[j]);
            }
            
        }
        close_file();
       
        return comp_image ;
        
    }
    
    public  int [][] originalImage ;
    
    public VectorQuantizaion(){
    	initComponents() ; 
    }
    
    @SuppressWarnings("unchecked") 
    private void initComponents() {
    	this.setSize(500, 300);
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setTitle("Vector Quantization");
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        JButton jButton1 = new javax.swing.JButton();
        JButton jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jLabel2.setText("Enter The Width of block : ");

        jLabel3.setText("Enter The Height of block : ");

        jLabel1.setText("Enter The Number of levels : ");

        

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jButton1.setText("Compression");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Decompression");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(50, 50, 50)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jTextField2)
                            .addComponent(jTextField3)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(jButton1)
                        .addGap(75, 75, 75)
                        .addComponent(jButton2)))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGap(55, 55, 55)

                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(99, Short.MAX_VALUE))
        );

        pack();
    }

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
    }
    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {}

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {

            Decompress ();  }
     
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
          
        int numOfLevels = Integer.parseInt(jTextField1.getText()) ;
        int widthOfBlock = Integer.parseInt(jTextField2.getText()) ;
        int heightOfBlock = Integer.parseInt(jTextField3.getText()) ;
        originalImage  = ReadImage("C:\\Users\\alaaf\\eclipse-workspace\\LastVersionOfVectorQuantization/alaa2.jpg");
     
        int numOfRows = originalImage.length /heightOfBlock ; // lel new matrix li mtkwna mn vectors 
        int numOfCols = originalImage[0].length /heightOfBlock ; 
        vector [][] vectors = new vector [numOfRows][numOfCols]; // 2D array consist of vectors 
      
        ArrayList <vector> data = Build_vectors (originalImage , vectors , numOfRows , numOfCols , widthOfBlock , heightOfBlock );
        Quantization (numOfLevels , data , widthOfBlock , heightOfBlock ,vectors , numOfRows , numOfCols  );
       
        
    }
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VectorQuantizaion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VectorQuantizaion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VectorQuantizaion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VectorQuantizaion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VectorQuantizaion().setVisible(true);
            }
        });
    }
    class vector 
    {  
        int width ;
        int height ;
        double [][] data ;
        
        public vector () {}
        public vector(int width, int height) {
            this.width = width;
            this.height = height;
            this.data = new double [height][width];
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double[][] getData() {
            return data;
        }

        public void setData(double[][] data) {
            this.data = data;
        }
        
        
    }
    
    class split_element 
    {
        vector value ;
        ArrayList<vector> assoicated = new ArrayList<>();
        
        public split_element() {}
        
        public split_element(vector value ,ArrayList<vector> assoicated ) {
            this.value = value;
            this.assoicated = assoicated ;
        }

        public vector getValue() {
            return value;
        }

        public void setValue(vector value) {
            this.value = value;
        }

        public ArrayList<vector> getAssoicated() {
            return assoicated;
        }

        public void setAssoicated(ArrayList<vector> assoicated) {
            this.assoicated = assoicated;
        }

        
    }
    
    
}

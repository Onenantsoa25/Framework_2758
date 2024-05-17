SET JAVAPATH = "C:\Program Files\Java\jdk1.8.0_202\bin\"
%JAVAPATH%javac -d D:\testFr "D:\Framework_2758\mg\itu\prom\*.java"
jar cvf "D:\testFr\mg.jar" -C "D:\testFr" mg

# Java-Utils

In this repository, I will keep uploading some Java utility classes that I've developed and used in my Closed Source projects.

### Table of Contents

 - [LongList](#longlist)
 - [ObjectStore](#objectstore)
 - [SimpleGraphView](#simplegraphview)
 - [TerraformBlockGenerator](#terraformblockgenerator)

## LongList

An `ArrayList` class is not feasible when you have to store a lot of heavy `Object`s in a list. `LongList` class can easily store more than 100,000 elements in the form of an array in a memory-efficient way without compromising too much on read/write speed. The way this works is, it stores all the elements on the disk (secondary storage) and only stores a fixed number of elements (called a "block") into the memory. Every time you are accessing an element in `LongList`, you'll be reading it from the block (which is stored in the memory) and not from the disk. Hence the speed is not compromised. For example, if you have 500,000 elements stored in `LongList` and the block size that you've defined is 1000, then you'll be consuming the memory only for those 1000 elements and the rest of the elements will be on the disk.

### Code example

 - Initialize a `LongList`
```java
/*  
* providing a unique ID to each LongList is required.  
* Block size is optional. default block size is 500.  
* */  
//'Car' is a sample user-defined class
LongList<Car> carLongList = new LongList<>("car-list", 1000);  
```
 - Add an element to `LongList`
```java
carLongList.add(new Car());
```

- Initialize a `LongList` with an `ArrayList`
```java
carLongList.initialize(carArrayList); 
```
- Access elements from `LongList`
```java
for(int i=0; i<carLongList.size(); i++){ 	
	System.out.println(carLongList.get(i));  
}
```
- Data stored in `LongList` is persistent. To clear the data from a `LongList`
```java
carLongList.clear(); 
``` 
This can also be done like this:
```java
LongList.clear("car-list");
``` 
- You can choose where you want `LongList` to store the data on disk. You can provide the path like this:
```java
// getCacheDir() gives you the cache directory in Android
LongList.storageRootDir = getCacheDir().toString();   
```
Current working directory is the default path where the `LongList` will store the data.


## ObjectStore

This class stores Java Objects on the disk. Uses LRUMap internally. I created this utility class to display 'Recently viewed TV shows' list shown below.

<img height="500" src="https://raw.githubusercontent.com/NandanDesai/res/master/usage-of-objectstore.png">



## SimpleGraphView



This class can be used to create Graphs (Network graphs) for Android. I didn't find any suitable library for creating a network graph for Android and so I decided to create my own. This file has no other dependency. Just copy the file in your project and include the following in your layout (xml file) where required:

```xml
<io.github.nandandesai.datamonitr.graphics.SimpleGraphView  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"  
  android:id="@+id/networkGraph" />
```

(The name of the tag changes based on the package name where you place the SimpleGraphView.java file).


And then, you can build the graph as shown below:

```java
List<SimpleGraphView.Node> nodeList = new ArrayList<>();  
List<SimpleGraphView.Edge> edgeList = new ArrayList<>();  
  
//Source-1  
SimpleGraphView.Node sourceNode1 = new SimpleGraphView.Node(getResources().getDrawable(R.drawable.laptop_sample, null), "Source-1");  
nodeList.add(sourceNode1);  
  
//Destination-1  
SimpleGraphView.Node destNode1 = new SimpleGraphView.Node(getResources().getDrawable(R.drawable.server_icon, null), "Destination-1");  
nodeList.add(destNode1);  
  
//Destination-2  
SimpleGraphView.Node destNode2 = new SimpleGraphView.Node(getResources().getDrawable(R.drawable.server_icon, null), "Destination-2");  
nodeList.add(destNode2);  
  
//Destination-3  
SimpleGraphView.Node destNode3 = new SimpleGraphView.Node(getResources().getDrawable(R.drawable.server_icon, null), "Destination-3");  
nodeList.add(destNode3);  
  
//Edge connecting Source-1 and Destination-1  
SimpleGraphView.Edge edge1 = new SimpleGraphView.Edge(sourceNode1, destNode1);  
edge1.setUpperText("Source-1 to Destination-1");  
edge1.setLowerText("More info");  
edgeList.add(edge1);  
  
//Edge connecting Source-1 and Destination-2  
SimpleGraphView.Edge edge2 = new SimpleGraphView.Edge(sourceNode1, destNode2);  
edge2.setUpperText("Source-1 to Destination-2");  
edge2.setLowerText("More info 2");  
edgeList.add(edge2);  
  
//Edge connecting Source-1 and Destination-3  
SimpleGraphView.Edge edge3 = new SimpleGraphView.Edge(sourceNode1, destNode3);  
edge3.setUpperText("Source-1 to Destination-3");  
edge3.setLowerText("More info 3");  
edgeList.add(edge3);  
  
//create a Graph object  
SimpleGraphView.Graph graph = new SimpleGraphView.Graph();  
  
//add all edges to the graph object  
graph.addAllEdges(edgeList);  
  
//add all nodes to the graph object  
graph.addAllNodes(nodeList);  
  
//find the SimpleGraphView  
SimpleGraphView simpleGraphView = findViewById(R.id.networkGraph);  
  
//submit graph object and let the SimpleGraphView draw it  
simpleGraphView.draw(graph);
```


## TerraformBlockGenerator

I wanted to generate the Terraform configuration code in [HCL](https://www.terraform.io/language/syntax/configuration) and not in JSON. I wrote this simple Java class to do that. TerraformBlockGenerator.java file doesn't have any dependency. Just include the file in your project and you're good to go.

### Example

Java code:

```java
Map<String,Object> tfAttributes = new HashMap<>();  
tfAttributes.put("ami", "#expdata.aws_ami.ubuntu.id");  
tfAttributes.put("instance_type", "t3.micro");  
  
Map<String, String> tags = new HashMap<>();  
tags.put("Name", "Hello World");  
  
tfAttributes.put("tags", tags);  
  
String tfResourceBlock = new TerraformBlockGenerator().generate("resource", "aws_instance", "web", tfAttributes);  
System.out.println(tfResourceBlock);
```

Output of the above code:

```hcl
resource "aws_instance" "web" {
	ami = data.aws_ami.ubuntu.id
	instance_type = "t3.micro"
	tags {
		Name = "Hello World"
	}
}
```


## License 

Copyright 2024 Moustapha Iradukunda
Licensed under the Apache License, Version 2.0 (the "License");
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
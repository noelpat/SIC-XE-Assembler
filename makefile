# makefile for Project 1
# compiles java source code
# Reference: https://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html

#define variables

JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

CLASSES = \
	Symbol.java \
	SicOpTable.java \
	SicOp.java \
	Register.java \
	Pass1.java \
	Pass2.java \
	LabelTable.java \
	Helpers.java \
	Displacement.java \
	objMath.java \
	useBlock.java \
	project4.java

default: classes

classes: $(CLASSES:.java=.class)

#run make clean to remove class files to start over
clean:
	$(RM) *.class

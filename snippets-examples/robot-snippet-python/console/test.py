#!/usr/bin/python
import sys
print ('Number of Arguments:', len(sys.argv), 'arguments.')
print ('Argument List:', str(sys.argv))
print('This is Python Code')
print('Executing Python')
print('From Appian RPA')

f= open("C:/Users/javier.advani/Desktop/appian_python_output.txt","w+")
f.write("Appian RPA is able to write a file using Python!\n")
for i in range(5):
     f.write("This is the line %d" % (i+1)+";\r\n")
f.close()
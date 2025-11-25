# Calinea Playground

This is a playground project for experimenting with the Calinea library.
It's mainly intended for testing purposes. But you can also use it as a simple way to try out Calinea features.

## Commands

If you are using the `calinea-playground` plugin, you can use some basic features in-game:

- ```/calinea center <component> [width] [mustBeResolved]``` - Center text in chat.
- ```/calinea left <component> [width] [mustBeResolved]``` - Align text to the left.
- ```/calinea right <component> [width] [mustBeResolved]``` - Align text to the right.
- ```/calinea measure <component> [mustBeResolved]``` - Get the width of the text.
- ```/calinea align <left|right|center> <component> [width] [mustBeResolved]``` - General alignment command.
- ```/calinea examples <exampleName> [width]``` - Show some example usages.

### Examples
```java
/calinea center "Welcome to the Server"
```  
```java
/calinea right "Page 1/5" 320
```  
```java
/calinea measure "How wide am I?"
```  
```java
/calinea align left "I'm a very long text that should be aligned to the left side of the chat window." 100
```  
```java
/calinea left {"text":"Hey ", extra:[{selector:"@s",color:gold},{text:"\nI'm very happy today :D",color:green}]} 100 true
```
```java
/calinea left {"text":"Hey ", extra:[{selector:"@s",color:gold},{text:"\nI'm very happy today :D",color:green}]} 100 true
```
```java
/calinea examples aliceLetter
```

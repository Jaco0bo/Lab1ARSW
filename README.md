# Laboratorio 1 - Arquitecturas del Software (ARSW)
## Andrés Jacobo Sepúlveda Sánchez

### Parte 1 Introducción a Hilos en Java

**1.**

![CountThread](images/CountThread.png)

**2.1** 

![CountThreadMain](images/CountThreadMain.png)

**2.2**

![CountThreadMain2](images/CountThreadMain2.png)

**2.3**

![2-3](images/2-3.png)

**2.4** 

Cuando cambiamos start() por run() la salida en este caso se muestra en orden. Esto se debe a que con run() no se ejecutan todos los hilos de forma paralela si no que se ejecuta de manera bloqueante, es decir, uno detras del otro lo que implica que hasta que el primer hilo no acabe el segundo no se va a ejecutar. 

![2-4](images/2-4.png)

### Parte 2 Ejercicio Black List Search






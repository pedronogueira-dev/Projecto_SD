# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 39 - Campus Alameda


Pedro Nogueira 67055 17pedro.n@gmail.com

Joao Miguens 79201 jp_miguens_adidas@hotmail.com

Joao Pestana 79515 joaovascopestana@gmail.com



Repositório:
[tecnico-distsys/A_39-project](https://github.com/tecnico-distsys/a_39-project/)


-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

```
 Linux
```



[1] Iniciar servidores de apoio

JUDDI:

```
cd Desktop/juddi-3.3.2_tomcat-7.0.64_9090/bin

./startup.sh
```



[2] Criar pasta temporária


```
cd Desktop
mkdir tempFolder
```



[3] Obter código fonte do projeto (versão entregue)

```
git clone https://github.com/tecnico-distsys/A_39-project 
git checkout tags/
```



[4] Instalar módulos de bibliotecas auxiliares


```
cd SD/uddi-naming
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**


```
cd SD/A_39-project/transporter-ws
mvn clean generate-sources install
mvn exec:java
mvn -Dws.i=2 exec:java
mvn -Dws.i=3 exec:java
```


[2] Construir **cliente** e executar testes


```
cd ../transporter-ws-cli
mvn clean generate-sources install
mvn exec:java
```




-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir

```
cd ../broker-ws
mvn clean generate-sources install

```

[2] Executar o executar **servidor backup**

```
mvn -Dws.j=9 exec:java
```

[3] Executar o executar **servidor backup**

```
mvn exec:java
```

[2] Construir **cliente** e executar testes

```
cd ../broker-ws-cli
mvn clean install
mvn exec:java
```



-------------------------------------------------------------------------------
**FIM**```


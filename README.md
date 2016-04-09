# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 39 - Campus Alameda
*(preencher com número do grupo de SD no Fénix e depois apagar esta linha)*

Pedro Nogueira 67055 17pedro.n@gmail.com

Joao Miguens 79201 jp_miguens_adidas@hotmail.com

Joao Pestana 79515 joaovascopestana@gmail.com
*(preencher com nome, número e email de membro do grupo e depois apagar esta linha)*


Repositório:
[tecnico-distsys/a_39-project](https://github.com/tecnico-distsys/a_39-project/)


-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Indicar Windows ou Linux
*(escolher um dos dois, que esteja disponível nos laboratórios, e depois apagar esta linha)*


[1] Iniciar servidores de apoio

JUDDI:
```
...
```


[2] Criar pasta temporária

```
cd ...
mkdir ...
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone ... 
```
*(colocar aqui comandos git para obter a versão entregue a partir da tag e depois apagar esta linha)*


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```

```
cd ...
mvn clean install
```


-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd ...-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes

```
cd ...-ws-cli
mvn clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd ...-ws
mvn clean install
mvn exec:java
```


[2] Construir **cliente** e executar testes

```
cd ...-ws-cli
mvn clean install
```

...

-------------------------------------------------------------------------------
**FIM**

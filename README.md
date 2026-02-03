Como rodar o projeto localmente
1. Instalações:
Java JDK 17 (ou superior)
Maven

2. Configuração do Banco de Dados
O projeto utiliza o banco de dados configurado no arquivo src/main/resources/application.properties

3. Comandos de Execução
No terminal, dentro da pasta raiz do projeto, execute:

Bash
mvn clean install
mvn spring-boot:run

4. Acessar a Aplicação
Após o Spring Boot iniciar, abra o seu navegador e acesse:

Site: http://localhost:8080

Banco de Dados (H2): http://localhost:8080/h2-console

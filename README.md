# TDE 08 — WebService REST de Pacientes

## Objetivo

Implementar as operações CRUD da classe `Paciente` inspecionada no trabalho anterior, com Spring Boot, Spring Data JPA e uma API REST. A interface HTML/JavaScript do TDE 07 foi reaproveitada para testar as operações pelo navegador.

## Organização

- `model/Paciente.java`: entidade do TDE 07 e validações de domínio.
- `repository/PacienteRepository.java`: persistência JPA e consultas de unicidade.
- `service/PacienteService.java`: regras de negócio, resolução dos relacionamentos e transações.
- `controller/PacienteController.java`: endpoints HTTP do CRUD.
- `dto/`: contrato JSON de entrada e saída, compatível com `PacienteCtrl.js`.
- `OpcoesController` e `OpcoesService`: consultas auxiliares para preencher os campos do formulário.
- `src/main/resources/static/`: HTML e JavaScript reaproveitados do TDE 07.
- `src/test/java/app/`: testes de Repository, Service e RestController.

Fluxo: `paciente.html → PacienteCtrl.js → PacienteApi.js → PacienteController → PacienteService → PacienteRepository → H2`.

## Como executar

Requisitos: JDK 17 ou superior e Maven 3.6.3 ou superior, com acesso às dependências do Maven Central. Na pasta que contém `pom.xml`:

```bash
mvn clean test
mvn spring-boot:run
```

Abra http://localhost:8080/paciente.html. A aplicação serve a própria interface, portanto não é necessário abrir o HTML separadamente ou configurar CORS. O banco H2 em memória é criado na inicialização com três opções fictícias de cada entidade relacionada, sem pacientes cadastrados. Os dados são descartados ao encerrar a aplicação.

## Endpoints

| Método | Caminho | Resultado em caso de sucesso |
|---|---|---|
| POST | /api/pacientes | 201 Created |
| GET | /api/pacientes | 200 OK |
| GET | /api/pacientes/{id} | 200 OK |
| PUT | /api/pacientes/{id} | 200 OK |
| DELETE | /api/pacientes/{id} | 204 No Content |

Consultas auxiliares: GET `/api/planos`, `/api/registros-pessoa`, `/api/usuarios`, `/api/consultas` e `/api/aplicacoes-dose`.

Exemplo de corpo para POST ou PUT (use os IDs efetivamente retornados pelos endpoints auxiliares):

```json
{
  "tipoSanguineo": "A+",
  "numeroSus": "12345678901234",
  "planoId": 1,
  "registroPessoaId": 1,
  "usuarioId": 1,
  "consultaIds": [1],
  "aplicacaoDoseIds": [1]
}
```

A resposta acrescenta `id` aos mesmos campos. O SUS possui 14 dígitos conforme a entidade e a interface do TDE 07. A API retorna 400 para dados inválidos, 404 para registros inexistentes e 409 para conflitos de unicidade ou relacionamentos já ocupados. O campo `message` dos erros é lido pelo JavaScript original.

## Testes

| Classe | Cenários implementados |
|---|---|
| PacienteRepositoryTest | Persistência, consulta, atualização, exclusão e restrição de SUS único |
| PacienteServiceTest | CRUD, manutenção dos relacionamentos, SUS duplicado, consulta já vinculada, tipo sanguíneo inválido, paciente inexistente |
| PacienteControllerTest | CRUD via MockMvc, HTTP 400/404/409 e endpoints auxiliares do TDE 07 |

Teste manual: na página, cadastre um paciente selecionando plano, registro e usuário; escolha consultas/doses se desejar; confira a tabela, edite o cadastro, atualize a lista e exclua o paciente. Cadastre dois pacientes com planos e registros distintos e tente repetir o SUS para conferir o erro. Ao excluir, as entidades selecionadas devem continuar disponíveis para outro cadastro.

**Estado da validação:** os testes foram escritos, mas não executados no ambiente de preparação, que não possui Maven e não consegue acessar o Maven Central. Não há resultado de execução Java confirmado. Os scripts JavaScript foram verificados sintaticamente com Node.js.

## Decisões e limites da entrega

- Foram fornecidos apenas `Paciente.java`, `PacienteApi.js`, `PacienteCtrl.js` e `paciente.html`. Para permitir uma demonstração independente, foram criadas versões mínimas de `Plano`, `RegistroPessoa`, `Usuario`, `Consulta`, `AplicacaoDose`, `TipoSanguineo` e `ModelException`. Não representam as implementações originais dessas classes. Ao integrar ao projeto da turma, use as entidades reais e adapte a conversão do enum, os métodos auxiliares e as consultas conforme necessário.
- O modelo `Paciente` preserva os atributos, tipos, nomes e validações recebidos. A única mudança estrutural foi remover `CascadeType.ALL` dos quatro relacionamentos que apontam para registros previamente existentes. Assim, excluir um paciente não exclui plano, registro, consulta ou dose. Na demonstração, consultas e doses podem ficar sem paciente; o serviço desvincula os registros antes da exclusão. Se as entidades reais exigirem paciente obrigatório, será preciso decidir com o projeto original entre rejeitar o DELETE com vínculos ou outra regra de exclusão explicitamente aprovada.
- O tipo sanguíneo usa nomes de enum de até três caracteres para respeitar a coluna original, mas a API recebe/devolve os rótulos do formulário (A+, AB-, etc.).
- A interface original foi preservada, alterando somente o endereço da API para `/api` e textos que se referiam aos endpoints futuros. O usuário cadastrante é escolhido pelo formulário conforme o TDE 07; este projeto acadêmico não implementa autenticação/autorização, concorrência avançada ou requisitos de segurança de uma aplicação clínica real. Utilize apenas dados fictícios.

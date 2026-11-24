// Controlador da interface: eventos, campos, validações simples e renderização.
// O acesso à API fica exclusivamente em PacienteApi.js.
class PacienteCtrl {
  constructor() {
    this.api = new PacienteApi('/api');
    this.pacientes = [];
    this.opcoesCarregadas = false;
    this.form = document.getElementById('paciente-form');
    this.id = document.getElementById('paciente-id');
    this.tipoSanguineo = document.getElementById('tipo-sanguineo');
    this.numeroSus = document.getElementById('numero-sus');
    this.planoId = document.getElementById('plano-id');
    this.registroPessoaId = document.getElementById('registro-pessoa-id');
    this.usuarioId = document.getElementById('usuario-id');
    this.consultasOpcoes = document.getElementById('consultas-opcoes');
    this.aplicacoesOpcoes = document.getElementById('aplicacoes-opcoes');
    this.tabela = document.getElementById('pacientes-corpo');
    this.mensagem = document.getElementById('mensagem');
    this.salvarBtn = document.getElementById('salvar-btn');
    this.cancelarBtn = document.getElementById('cancelar-btn');
    this.atualizarBtn = document.getElementById('atualizar-btn');
    this.formTitulo = document.getElementById('form-titulo');

    this.form.addEventListener('submit', (evento) => this.salvar(evento));
    this.cancelarBtn.addEventListener('click', () => this.limparFormulario());
    this.atualizarBtn.addEventListener('click', () => this.atualizarLista());
    this.tabela.addEventListener('click', (evento) => this.acaoTabela(evento));
    this.inicializar();
  }

  obterLista(resposta) {
    if (Array.isArray(resposta)) return resposta;
    if (Array.isArray(resposta?.content)) return resposta.content;
    throw new Error('A API deve retornar uma lista de registros.');
  }

  obterId(registro) {
    return registro && typeof registro === 'object' ? registro.id : registro;
  }

  idRelacionamento(paciente, chave, objeto) {
    return paciente[chave] ?? this.obterId(paciente[objeto]) ?? '';
  }

  exibirMensagem(texto, erro = false) {
    this.mensagem.textContent = texto;
    this.mensagem.className = erro ? 'mensagem erro' : 'mensagem';
  }

  bloquearAcoes(bloquear) {
    this.salvarBtn.disabled = bloquear || !this.opcoesCarregadas;
    this.atualizarBtn.disabled = bloquear;
  }

  async inicializar() {
    this.bloquearAcoes(true);
    try {
      const [pacientes, planos, registros, usuarios, consultas, aplicacoes] = await Promise.all([
        this.api.listarPacientes(), this.api.listarPlanos(), this.api.listarRegistrosPessoa(),
        this.api.listarUsuarios(), this.api.listarConsultas(), this.api.listarAplicacoesDose()
      ]);
      this.pacientes = this.obterLista(pacientes);
      this.popularSelect(this.planoId, this.obterLista(planos), 'Selecione um plano');
      this.popularSelect(this.registroPessoaId, this.obterLista(registros), 'Selecione um registro');
      this.popularSelect(this.usuarioId, this.obterLista(usuarios), 'Selecione um usuário');
      this.popularCheckboxes(this.consultasOpcoes, this.obterLista(consultas), 'consulta');
      this.popularCheckboxes(this.aplicacoesOpcoes, this.obterLista(aplicacoes), 'aplicacao');
      this.opcoesCarregadas = true;
      this.renderizarPacientes();
      this.exibirMensagem('Dados carregados com sucesso.');
    } catch (erro) {
      this.exibirMensagem(`${erro.message}`, true);
      this.mostrarTabelaVazia('Não foi possível carregar os pacientes.');
    } finally {
      this.bloquearAcoes(false);
    }
  }

  nomeOpcao(item) {
    return item.nome || item.descricao || item.titulo || item.numero ||
      item.pessoa?.nome || `Registro #${item.id}`;
  }

  popularSelect(select, itens, instrucao) {
    select.replaceChildren();
    const inicial = document.createElement('option');
    inicial.value = '';
    inicial.textContent = instrucao;
    select.appendChild(inicial);
    for (const item of itens) {
      const opcao = document.createElement('option');
      opcao.value = String(item.id);
      opcao.textContent = `${this.nomeOpcao(item)} (ID ${item.id})`;
      select.appendChild(opcao);
    }
    select.disabled = false;
  }

  popularCheckboxes(container, itens, prefixo) {
    container.replaceChildren();
    if (!itens.length) {
      container.textContent = 'Nenhum registro disponível.';
      return;
    }
    for (const item of itens) {
      const rotulo = document.createElement('label');
      rotulo.className = 'opcao';
      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox';
      checkbox.name = prefixo;
      checkbox.value = String(item.id);
      rotulo.append(checkbox, document.createTextNode(` ${this.nomeOpcao(item)} (ID ${item.id})`));
      container.appendChild(rotulo);
    }
  }

  valoresMarcados(container) {
    return [...container.querySelectorAll('input[type="checkbox"]:checked')].map((item) => Number(item.value));
  }

  montarDados() {
    return {
      tipoSanguineo: this.tipoSanguineo.value,
      numeroSus: this.numeroSus.value.trim(),
      planoId: Number(this.planoId.value),
      registroPessoaId: Number(this.registroPessoaId.value),
      usuarioId: Number(this.usuarioId.value),
      consultaIds: this.valoresMarcados(this.consultasOpcoes),
      aplicacaoDoseIds: this.valoresMarcados(this.aplicacoesOpcoes)
    };
  }

  async salvar(evento) {
    evento.preventDefault();
    if (!this.opcoesCarregadas || !this.form.reportValidity()) return;
    if (!/^\d{14}$/.test(this.numeroSus.value.trim())) {
      this.exibirMensagem('O número do SUS deve ter exatamente 14 dígitos.', true);
      return;
    }
    const id = this.id.value;
    this.bloquearAcoes(true);
    try {
      if (id) {
        await this.api.atualizarPaciente(id, this.montarDados());
      } else {
        await this.api.cadastrarPaciente(this.montarDados());
      }
      this.limparFormulario();
      await this.carregarPacientes();
      this.exibirMensagem(id ? 'Paciente atualizado com sucesso.' : 'Paciente cadastrado com sucesso.');
    } catch (erro) {
      this.exibirMensagem(erro.message, true);
    } finally {
      this.bloquearAcoes(false);
    }
  }

  async carregarPacientes() {
    this.pacientes = this.obterLista(await this.api.listarPacientes());
    this.renderizarPacientes();
  }

  async atualizarLista() {
    this.bloquearAcoes(true);
    try {
      await this.carregarPacientes();
      this.exibirMensagem('Lista atualizada.');
    } catch (erro) {
      this.exibirMensagem(erro.message, true);
    } finally {
      this.bloquearAcoes(false);
    }
  }

  mostrarTabelaVazia(texto) {
    this.tabela.replaceChildren();
    const linha = document.createElement('tr');
    const celula = document.createElement('td');
    celula.colSpan = 9;
    celula.textContent = texto;
    linha.appendChild(celula);
    this.tabela.appendChild(linha);
  }

  renderizarPacientes() {
    this.tabela.replaceChildren();
    if (!this.pacientes.length) {
      this.mostrarTabelaVazia('Nenhum paciente cadastrado.');
      return;
    }
    for (const paciente of this.pacientes) {
      const linha = document.createElement('tr');
      const campos = [
        paciente.id, paciente.numeroSus, paciente.tipoSanguineo,
        this.idRelacionamento(paciente, 'planoId', 'plano'),
        this.idRelacionamento(paciente, 'registroPessoaId', 'registroPessoa'),
        this.idRelacionamento(paciente, 'usuarioId', 'usuario'),
        (paciente.consultaIds ?? paciente.listaConsultas ?? []).length,
        (paciente.aplicacaoDoseIds ?? paciente.listaAplicacoes ?? []).length
      ];
      for (const campo of campos) {
        const celula = document.createElement('td');
        celula.textContent = String(campo ?? '—');
        linha.appendChild(celula);
      }
      const celulaAcoes = document.createElement('td');
      celulaAcoes.className = 'acoes-tabela';
      for (const [acao, texto] of [['editar', 'Editar'], ['excluir', 'Excluir']]) {
        const botao = document.createElement('button');
        botao.type = 'button';
        botao.dataset.acao = acao;
        botao.dataset.id = paciente.id;
        botao.textContent = texto;
        celulaAcoes.appendChild(botao);
      }
      linha.appendChild(celulaAcoes);
      this.tabela.appendChild(linha);
    }
  }

  acaoTabela(evento) {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    if (botao.dataset.acao === 'editar') this.editar(botao.dataset.id);
    if (botao.dataset.acao === 'excluir') this.excluir(botao.dataset.id);
  }

  editar(id) {
    const paciente = this.pacientes.find((item) => String(item.id) === String(id));
    if (!paciente) return;
    this.id.value = paciente.id;
    this.tipoSanguineo.value = paciente.tipoSanguineo || '';
    this.numeroSus.value = paciente.numeroSus || '';
    this.planoId.value = this.idRelacionamento(paciente, 'planoId', 'plano');
    this.registroPessoaId.value = this.idRelacionamento(paciente, 'registroPessoaId', 'registroPessoa');
    this.usuarioId.value = this.idRelacionamento(paciente, 'usuarioId', 'usuario');
    this.marcarRelacionamentos(this.consultasOpcoes, paciente.consultaIds ?? paciente.listaConsultas ?? []);
    this.marcarRelacionamentos(this.aplicacoesOpcoes, paciente.aplicacaoDoseIds ?? paciente.listaAplicacoes ?? []);
    this.formTitulo.textContent = `Editar paciente #${paciente.id}`;
    this.salvarBtn.textContent = 'Salvar alterações';
    this.cancelarBtn.hidden = false;
    this.form.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  marcarRelacionamentos(container, itens) {
    const ids = new Set(itens.map((item) => String(this.obterId(item))));
    for (const checkbox of container.querySelectorAll('input[type="checkbox"]')) {
      checkbox.checked = ids.has(checkbox.value);
    }
  }

  limparFormulario() {
    this.form.reset();
    this.id.value = '';
    this.formTitulo.textContent = 'Novo paciente';
    this.salvarBtn.textContent = 'Cadastrar paciente';
    this.cancelarBtn.hidden = true;
  }

  async excluir(id) {
    if (!confirm(`Deseja realmente excluir o paciente #${id}?`)) return;
    this.bloquearAcoes(true);
    try {
      await this.api.excluirPaciente(id);
      if (String(this.id.value) === String(id)) this.limparFormulario();
      await this.carregarPacientes();
      this.exibirMensagem('Paciente excluído com sucesso.');
    } catch (erro) {
      this.exibirMensagem(erro.message, true);
    } finally {
      this.bloquearAcoes(false);
    }
  }
}

new PacienteCtrl();

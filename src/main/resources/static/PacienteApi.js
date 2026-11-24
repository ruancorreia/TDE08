// Comunicação HTTP do front-end com os endpoints REST (TDE #08).
// Nenhuma regra de persistência é executada no navegador.
class PacienteApi {
  constructor(baseUrl = '/api') {
    this.baseUrl = baseUrl.replace(/\/$/, '');
  }

  async requisitar(caminho, metodo = 'GET', dados = undefined) {
    const opcoes = { method: metodo, headers: { Accept: 'application/json' } };
    if (dados !== undefined) {
      opcoes.headers['Content-Type'] = 'application/json';
      opcoes.body = JSON.stringify(dados);
    }

    let resposta;
    try {
      resposta = await fetch(`${this.baseUrl}${caminho}`, opcoes);
    } catch {
      throw new Error('Não foi possível conectar ao servidor REST.');
    }

    if (!resposta.ok) {
      let detalhe = '';
      try {
        const erro = await resposta.json();
        detalhe = erro.message || erro.mensagem || erro.error || '';
      } catch {
        // O servidor pode retornar um erro sem corpo JSON.
      }
      throw new Error(detalhe || `Erro HTTP ${resposta.status} ao acessar ${caminho}.`);
    }

    if (resposta.status === 204) return null;
    const conteudo = await resposta.text();
    return conteudo ? JSON.parse(conteudo) : null;
  }

  listarPacientes() { return this.requisitar('/pacientes'); }
  cadastrarPaciente(dados) { return this.requisitar('/pacientes', 'POST', dados); }
  atualizarPaciente(id, dados) { return this.requisitar(`/pacientes/${encodeURIComponent(id)}`, 'PUT', dados); }
  excluirPaciente(id) { return this.requisitar(`/pacientes/${encodeURIComponent(id)}`, 'DELETE'); }

  listarPlanos() { return this.requisitar('/planos'); }
  listarRegistrosPessoa() { return this.requisitar('/registros-pessoa'); }
  listarUsuarios() { return this.requisitar('/usuarios'); }
  listarConsultas() { return this.requisitar('/consultas'); }
  listarAplicacoesDose() { return this.requisitar('/aplicacoes-dose'); }
}

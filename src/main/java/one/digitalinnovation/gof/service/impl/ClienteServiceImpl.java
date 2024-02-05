package one.digitalinnovation.gof.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

@Service
public class ClienteServiceImpl implements ClienteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClienteServiceImpl.class);

	private final ClienteRepository clienteRepository;
	private final EnderecoRepository enderecoRepository;
	private final ViaCepService viaCepService;

	public ClienteServiceImpl(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository,
			ViaCepService viaCepService) {
		this.clienteRepository = clienteRepository;
		this.enderecoRepository = enderecoRepository;
		this.viaCepService = viaCepService;
	}

	@Override
	public Iterable<Cliente> buscarTodos() {
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		Optional<Cliente> clienteOpt = clienteRepository.findById(id);
		return clienteOpt.orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o ID: " + id));
	}

	@Override
	public void inserir(Cliente cliente) {
		salvarClienteComCep(cliente);
	}

	@Override
	public void atualizar(Long id, Cliente clienteAtualizado) {
		Optional<Cliente> clienteExistenteOpt = clienteRepository.findById(id);
		if (clienteExistenteOpt.isPresent()) {
			salvarClienteComCep(clienteAtualizado);
		} else {
			LOGGER.warn("Não foi possível atualizar o cliente com ID: {} porque ele não existe.", id);
		}
	}

	@Override
	public void deletar(Long id) {
		clienteRepository.deleteById(id);
	}

	private void salvarClienteComCep(Cliente cliente) {
		if (cliente.getEndereco() == null) {
			throw new IllegalArgumentException("O endereço do cliente não pode ser nulo.");
		}

		String cep = cliente.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});

		cliente.setEndereco(endereco);
		clienteRepository.save(cliente);
	}
}
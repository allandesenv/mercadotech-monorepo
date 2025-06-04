package com.mercadotech.stockservice.service;

import com.mercadotech.stockservice.client.ProductServiceFeignClient;
import com.mercadotech.stockservice.dto.ProdutoEstoqueDTO;
import com.mercadotech.stockservice.entity.EntradaEstoque;
import com.mercadotech.stockservice.entity.SaidaEstoque;
import com.mercadotech.stockservice.enums.TipoSaida;
import com.mercadotech.stockservice.repository.EntradaEstoqueRepository;
import com.mercadotech.stockservice.repository.SaidaEstoqueRepository;
import org.junit.jupiter.api.BeforeEach; // Importe para @BeforeEach
import org.junit.jupiter.api.Test; // Importe para @Test
import org.junit.jupiter.api.extension.ExtendWith; // Importe para @ExtendWith
import org.mockito.InjectMocks; // Importe para @InjectMocks
import org.mockito.Mock; // Importe para @Mock
import org.mockito.junit.jupiter.MockitoExtension; // Importe para MockitoExtension

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections; // Importe para Collections.emptyList()
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*; // Importe para asserções (assertEquals, assertThrows, etc.)
import static org.mockito.Mockito.*; // Importe para métodos estáticos do Mockito (when, verify)

@ExtendWith(MockitoExtension.class) // Habilita o Mockito para JUnit 5
public class EstoqueServiceTest {

    @Mock // Cria um mock da dependência
    private EntradaEstoqueRepository entradaEstoqueRepository;

    @Mock // Cria um mock da dependência
    private SaidaEstoqueRepository saidaEstoqueRepository;

    @Mock // Cria um mock da dependência
    private ProductServiceFeignClient productServiceFeignClient;

    @InjectMocks // Injeta os mocks criados nas dependências da classe a ser testada
    private EstoqueService estoqueService;

    // Dados de teste comuns
    private Long PRODUTO_ID = 1L;
    private ProdutoEstoqueDTO produtoValido;

    @BeforeEach // Este método é executado antes de cada teste
    void setUp() {
        // Inicializa um produto DTO válido para simular a resposta do product-service
        produtoValido = ProdutoEstoqueDTO.builder()
                .id(PRODUTO_ID)
                .name("Produto Teste")
                .price(BigDecimal.TEN)
                .categoryName("Categoria Teste")
                .build();
    }

    // --- Testes para registrarEntrada ---

    @Test
    void shouldRegisterEntradaSuccessfully() {
        // Given (Dado)
        EntradaEstoque entrada = EntradaEstoque.builder()
                .produtoId(PRODUTO_ID)
                .quantidade(50)
                .custoUnitario(new BigDecimal("10.00"))
                .observacao("Teste de entrada")
                .build();

        // Quando o product-service é chamado, retorne o produto válido
        when(productServiceFeignClient.getProductById(PRODUTO_ID)).thenReturn(Optional.of(produtoValido));
        // Quando o repositório salva, retorne a própria entrada (simulando salvamento)
        when(entradaEstoqueRepository.save(any(EntradaEstoque.class))).thenReturn(entrada);

        // When (Quando)
        EntradaEstoque result = estoqueService.registrarEntrada(entrada);

        // Then (Então)
        assertNotNull(result);
        assertEquals(50, result.getQuantidade());
        assertNotNull(result.getDataEntrada()); // Data deve ser preenchida automaticamente
        verify(productServiceFeignClient, times(1)).getProductById(PRODUTO_ID); // Verifica se o mock foi chamado
        verify(entradaEstoqueRepository, times(1)).save(entrada); // Verifica se o mock foi chamado
    }

    @Test
    void shouldThrowExceptionWhenRegisteringEntradaForNonExistingProduct() {
        // Given
        EntradaEstoque entrada = EntradaEstoque.builder()
                .produtoId(999L) // ID de produto inexistente
                .quantidade(10)
                .custoUnitario(new BigDecimal("5.00"))
                .build();

        // Quando o product-service é chamado para um ID inexistente, retorne Optional vazio
        when(productServiceFeignClient.getProductById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            estoqueService.registrarEntrada(entrada);
        });

        assertTrue(thrown.getMessage().contains("Produto com ID 999 não encontrado"));
        verify(productServiceFeignClient, times(1)).getProductById(999L);
        verify(entradaEstoqueRepository, never()).save(any(EntradaEstoque.class)); // Garante que o save nunca foi chamado
    }

    // --- Testes para registrarSaida ---

    @Test
    void shouldRegisterSaidaSuccessfully() {
        // Given
        SaidaEstoque saida = SaidaEstoque.builder()
                .produtoId(PRODUTO_ID)
                .quantidade(10)
                .tipoSaida(TipoSaida.VENDA)
                .observacao("Teste de saída")
                .build();

        // Simula saldo suficiente
        when(productServiceFeignClient.getProductById(PRODUTO_ID)).thenReturn(Optional.of(produtoValido));
        when(entradaEstoqueRepository.findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID))
                .thenReturn(Arrays.asList(EntradaEstoque.builder().quantidade(100).build())); // Simula 100 de entrada
        when(saidaEstoqueRepository.findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID))
                .thenReturn(Collections.emptyList()); // Simula 0 de saída até agora

        when(saidaEstoqueRepository.save(any(SaidaEstoque.class))).thenReturn(saida);

        // When
        SaidaEstoque result = estoqueService.registrarSaida(saida);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getQuantidade());
        assertNotNull(result.getDataSaida());
        verify(productServiceFeignClient, times(1)).getProductById(PRODUTO_ID);
        verify(saidaEstoqueRepository, times(1)).save(saida);
        verify(entradaEstoqueRepository, times(1)).findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID);
        verify(saidaEstoqueRepository, times(1)).findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID);
    }

    @Test
    void shouldThrowExceptionWhenRegisteringSaidaForNonExistingProduct() {
        // Given
        SaidaEstoque saida = SaidaEstoque.builder()
                .produtoId(999L)
                .quantidade(5)
                .tipoSaida(TipoSaida.PERDA)
                .build();

        when(productServiceFeignClient.getProductById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            estoqueService.registrarSaida(saida);
        });

        assertTrue(thrown.getMessage().contains("Produto com ID 999 não encontrado"));
        verify(productServiceFeignClient, times(1)).getProductById(999L);
        verify(saidaEstoqueRepository, never()).save(any(SaidaEstoque.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringSaidaWithInsufficientStock() {
        // Given
        SaidaEstoque saida = SaidaEstoque.builder()
                .produtoId(PRODUTO_ID)
                .quantidade(150) // Tenta retirar mais do que tem
                .tipoSaida(TipoSaida.VENDA)
                .build();

        // Simula saldo insuficiente (Ex: 100 de entrada e 0 de saída, tenta tirar 150)
        when(productServiceFeignClient.getProductById(PRODUTO_ID)).thenReturn(Optional.of(produtoValido));
        when(entradaEstoqueRepository.findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID))
                .thenReturn(Arrays.asList(EntradaEstoque.builder().quantidade(100).build()));
        when(saidaEstoqueRepository.findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID))
                .thenReturn(Collections.emptyList());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            estoqueService.registrarSaida(saida);
        });

        assertTrue(thrown.getMessage().contains("Saldo insuficiente"));
        verify(productServiceFeignClient, times(1)).getProductById(PRODUTO_ID);
        verify(saidaEstoqueRepository, never()).save(any(SaidaEstoque.class));
        verify(entradaEstoqueRepository, times(1)).findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID);
        verify(saidaEstoqueRepository, times(1)).findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID);
    }

    // --- Testes para calcularSaldoAtual ---

    @Test
    void shouldCalculateCurrentBalanceCorrectly() {
        // Given
        // Simula entradas e saídas
        List<EntradaEstoque> entradas = Arrays.asList(
                EntradaEstoque.builder().quantidade(100).build(),
                EntradaEstoque.builder().quantidade(50).build()
        );
        List<SaidaEstoque> saidas = Arrays.asList(
                SaidaEstoque.builder().quantidade(20).build(),
                SaidaEstoque.builder().quantidade(10).build()
        );

        when(entradaEstoqueRepository.findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID)).thenReturn(entradas);
        when(saidaEstoqueRepository.findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID)).thenReturn(saidas);

        // When
        Integer saldo = estoqueService.calcularSaldoAtual(PRODUTO_ID);

        // Then
        assertEquals(120, saldo); // 100 + 50 - 20 - 10 = 120
        verify(entradaEstoqueRepository, times(1)).findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID);
        verify(saidaEstoqueRepository, times(1)).findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID);
    }

    @Test
    void shouldReturnZeroBalanceForProductWithNoMovements() {
        // Given
        when(entradaEstoqueRepository.findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID))
                .thenReturn(Collections.emptyList());
        when(saidaEstoqueRepository.findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID))
                .thenReturn(Collections.emptyList());

        // When
        Integer saldo = estoqueService.calcularSaldoAtual(PRODUTO_ID);

        // Then
        assertEquals(0, saldo);
        verify(entradaEstoqueRepository, times(1)).findByProdutoIdOrderByDataEntradaAsc(PRODUTO_ID);
        verify(saidaEstoqueRepository, times(1)).findByProdutoIdOrderByDataSaidaAsc(PRODUTO_ID);
    }
}
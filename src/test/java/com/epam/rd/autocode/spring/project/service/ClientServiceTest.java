package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientStatsDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService")
class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private static final Long CLIENT_ID = 1L;
    private static final String EMAIL = "client@example.com";
    private static final String BOOK_NAME = "Clean Code";
    private static final Long BOOK_ID = 42L;
    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Client client;
    private ClientDTO clientDTO;
    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(CLIENT_ID)
                .email(EMAIL)
                .active(true)
                .balance(new BigDecimal("100.00"))
                .booksInBasket(new HashSet<>())
                .build();

        clientDTO = new ClientDTO();
        clientDTO.setEmail(EMAIL);

        book = new Book(BOOK_ID, BOOK_NAME, "genre", AgeGroup.TEEN, new BigDecimal("20.00"),
                LocalDate.now(), "author", 1, "characteristics", "description",
                Language.ENGLISH, new HashSet<>(), new ArrayList<>());

        bookDTO = new BookDTO();
        bookDTO.setName(BOOK_NAME);
    }

    @Nested
    @DisplayName("getAllClients()")
    class GetAllClients {

        @Test
        @DisplayName("maps every client and preserves total elements")
        void mapsAndPreservesTotal() {
            Client client2 = Client.builder().id(2L).email("other@example.com").build();
            ClientDTO dto2 = new ClientDTO();
            Page<Client> page = new PageImpl<>(List.of(client, client2), PAGE, 50);

            when(clientRepository.findAll(PAGE)).thenReturn(page);
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);
            when(modelMapper.map(client2, ClientDTO.class)).thenReturn(dto2);

            Page<ClientDTO> result = clientService.getAllClients(PAGE);

            assertThat(result.getContent()).containsExactly(clientDTO, dto2);
            assertThat(result.getTotalElements()).isEqualTo(50);
            assertThat(result.getPageable()).isEqualTo(PAGE);
        }

        @Test
        @DisplayName("returns empty page when repository is empty")
        void emptyRepository() {
            when(clientRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(), PAGE, 0));

            Page<ClientDTO> result = clientService.getAllClients(PAGE);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getClientByEmail()")
    class GetClientByEmail {

        @Test
        @DisplayName("returns mapped DTO for existing client")
        void returnsDto() {
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

            ClientDTO result = clientService.getClientByEmail(EMAIL);

            assertThat(result).isEqualTo(clientDTO);
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void notFound() {
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClientByEmail(EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Client not found");

            verifyNoInteractions(modelMapper);
        }
    }

    @Nested
    @DisplayName("updateClientByEmail()")
    class UpdateClientByEmail {

        @Test
        @DisplayName("maps DTO onto existing client, saves, and returns mapped result")
        void savesAndReturns() {
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            doNothing().when(modelMapper).map(any(Client.class), any(ClientDTO.class));
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

            ClientDTO result = clientService.updateClientByEmail(EMAIL, clientDTO);

            verify(modelMapper).map(client, clientDTO);
            verify(clientRepository).save(client);
            assertThat(result).isEqualTo(clientDTO);
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void notFound() {
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.updateClientByEmail(EMAIL, clientDTO))
                    .isInstanceOf(NotFoundException.class);

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteClientByEmail()")
    class DeleteClientByEmail {

        @Test
        @DisplayName("delegates to clientRepository.deleteByEmail")
        void delegates() {
            doNothing().when(clientRepository).deleteByEmail(EMAIL);

            clientService.deleteClientByEmail(EMAIL);

            verify(clientRepository).deleteByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("addClient()")
    class AddClient {

        @Test
        @DisplayName("maps DTO to new Client, saves, and returns mapped result")
        void mapsAndSaves() {
            when(clientRepository.save(any(Client.class))).thenReturn(client);
            doNothing().when(modelMapper).map(any(ClientDTO.class), any(Client.class));
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

            ClientDTO result = clientService.addClient(clientDTO);

            verify(modelMapper).map(eq(clientDTO), any(Client.class));
            verify(clientRepository).save(any(Client.class));
            assertThat(result).isEqualTo(clientDTO);
        }
    }

    @Nested
    @DisplayName("addBookToBasket()")
    class AddBookToBasket {

        @Test
        @DisplayName("adds book to client basket and saves")
        void addsAndSaves() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.of(book));

            clientService.addBookToBasket(CLIENT_ID, BOOK_NAME);

            assertThat(client.getBooksInBasket()).contains(book);
            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void clientNotFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.addBookToBasket(CLIENT_ID, BOOK_NAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found");

            verifyNoInteractions(bookRepository);
        }

        @Test
        @DisplayName("throws NotFoundException when book not found")
        void bookNotFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.addBookToBasket(CLIENT_ID, BOOK_NAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Book not found");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getBasketItemsById()")
    class GetBasketItemsById {

        @Test
        @DisplayName("maps all basket books to DTOs")
        void mapsAll() {
            when(clientRepository.findAllBooksInBasketById(CLIENT_ID)).thenReturn(Set.of(book));
            when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

            Set<BookDTO> result = clientService.getBasketItemsById(CLIENT_ID);

            assertThat(result).containsExactly(bookDTO);
        }

        @Test
        @DisplayName("returns empty set when basket is empty")
        void emptyBasket() {
            when(clientRepository.findAllBooksInBasketById(CLIENT_ID)).thenReturn(Set.of());

            Set<BookDTO> result = clientService.getBasketItemsById(CLIENT_ID);

            assertThat(result).isEmpty();
            verifyNoInteractions(modelMapper);
        }
    }

    @Nested
    @DisplayName("removeBookFromBasket()")
    class RemoveBookFromBasket {

        @Test
        @DisplayName("looks up book and delegates removal to repository")
        void delegates() {
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.of(book));

            clientService.removeBookFromBasket(CLIENT_ID, BOOK_NAME);

            verify(clientRepository).removeBookFromBasketByClientIdAndBookId(CLIENT_ID, BOOK_ID);
        }

        @Test
        @DisplayName("throws NotFoundException when book not found")
        void bookNotFound() {
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.removeBookFromBasket(CLIENT_ID, BOOK_NAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Book not found");

            verify(clientRepository, never()).removeBookFromBasketByClientIdAndBookId(any(), any());
        }
    }

    @Nested
    @DisplayName("clearBasket()")
    class ClearBasket {

        @Test
        @DisplayName("delegates to clientRepository.clearBasketByClientId")
        void delegates() {
            doNothing().when(clientRepository).clearBasketByClientId(CLIENT_ID);

            clientService.clearBasket(CLIENT_ID);

            verify(clientRepository).clearBasketByClientId(CLIENT_ID);
        }
    }

    @Nested
    @DisplayName("depositById()")
    class DepositById {

        @Test
        @DisplayName("adds deposit to client balance and saves")
        void addsToBalance() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

            clientService.depositById(CLIENT_ID, new BigDecimal("50.00"));

            assertThat(client.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void notFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.depositById(CLIENT_ID, BigDecimal.TEN))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Client not found");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("withdrawById()")
    class WithdrawById {

        @Test
        @DisplayName("subtracts amount from client balance and saves")
        void subtractsFromBalance() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

            clientService.withdrawById(CLIENT_ID, new BigDecimal("30.00"));

            assertThat(client.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
            verify(clientRepository).save(client);
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void notFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.withdrawById(CLIENT_ID, BigDecimal.TEN))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Client not found");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getClientStats()")
    class GetClientStats {

        @Test
        @DisplayName("delegates directly to repository and returns result")
        void delegates() {
            ClientStatsDTO stats = mock(ClientStatsDTO.class);
            when(clientRepository.getClientStats()).thenReturn(stats);

            ClientStatsDTO result = clientService.getClientStats();

            assertThat(result).isSameAs(stats);
        }
    }

    @Nested
    @DisplayName("toggleClientByEmail()")
    class ToggleClientByEmail {

        @Test
        @DisplayName("flips active=true to active=false and saves")
        void trueToFalse() {
            client.setActive(true);
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

            clientService.toggleClientByEmail(EMAIL);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isFalse();
        }

        @Test
        @DisplayName("flips active=false to active=true and saves")
        void falseToTrue() {
            client.setActive(false);
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

            clientService.toggleClientByEmail(EMAIL);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isTrue();
        }

        @Test
        @DisplayName("throws NotFoundException when client not found")
        void notFound() {
            when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.toggleClientByEmail(EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Client not found");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllClientsBySearch()")
    class GetAllClientsBySearch {

        @Test
        @DisplayName("lowercases search term before querying")
        void lowercasesSearch() {
            when(clientRepository.findAllBySearch(eq("john"), eq(PAGE)))
                    .thenReturn(new PageImpl<>(List.of(client), PAGE, 1));
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

            clientService.getAllClientsBySearch("JOHN", PAGE);

            verify(clientRepository).findAllBySearch("john", PAGE);
        }

        @Test
        @DisplayName("maps results and preserves total elements")
        void mapsAndPreservesTotal() {
            Page<Client> page = new PageImpl<>(List.of(client), PAGE, 25);
            when(clientRepository.findAllBySearch(anyString(), eq(PAGE))).thenReturn(page);
            when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

            Page<ClientDTO> result = clientService.getAllClientsBySearch("john", PAGE);

            assertThat(result.getContent()).containsExactly(clientDTO);
            assertThat(result.getTotalElements()).isEqualTo(25);
        }

        @Test
        @DisplayName("returns empty page when no clients match")
        void noResults() {
            when(clientRepository.findAllBySearch(anyString(), eq(PAGE)))
                    .thenReturn(new PageImpl<>(List.of(), PAGE, 0));

            Page<ClientDTO> result = clientService.getAllClientsBySearch("xyz", PAGE);

            assertThat(result.getContent()).isEmpty();
            verifyNoInteractions(modelMapper);
        }
    }
}
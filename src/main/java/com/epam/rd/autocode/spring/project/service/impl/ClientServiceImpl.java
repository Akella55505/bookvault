package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientStatsDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable) {
        Page<Client> clientsPage = clientRepository.findAll(pageable);
        List<ClientDTO> clientList = clientsPage.stream().map(client -> modelMapper.map(client, ClientDTO.class)).toList();
        return new PageImpl<>(clientList, pageable, clientsPage.getTotalElements());
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Client not found"));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO clientDto) {
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Client not found"));
        modelMapper.map(client, clientDto);
        client = clientRepository.save(client);
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        clientRepository.deleteByEmail(email);
    }

    @Override
    public ClientDTO addClient(ClientDTO clientDto) {
        Client client = new Client();

        modelMapper.map(clientDto, client);
        client = clientRepository.save(client);

        return modelMapper.map(client, ClientDTO.class);
    }

    @Transactional
    public void addBookToBasket(Long clientId, String bookName) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("User not found"));
        Book book = bookRepository.findByName(bookName).orElseThrow(() -> new NotFoundException("Book not found"));
        client.getBooksInBasket().add(book);
        clientRepository.save(client);
    }

    public Set<BookDTO> getBasketItemsById(Long clientId) {
        return clientRepository.findAllBooksInBasketById(clientId).stream().map(book -> modelMapper.map(book, BookDTO.class)).collect(Collectors.toSet());
    }

    @Transactional
    public void removeBookFromBasket(Long clientId, String bookName) {
        Book book = bookRepository.findByName(bookName).orElseThrow(() -> new NotFoundException("Book not found"));
        clientRepository.removeBookFromBasketByClientIdAndBookId(clientId, book.getId());
    }

    @Transactional
    public void clearBasket(Long clientId) {
        clientRepository.clearBasketByClientId(clientId);
    }

    @Transactional
    public void depositById(Long clientId, BigDecimal deposit) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found"));
        client.setBalance(client.getBalance().add(deposit));
        clientRepository.save(client);
    }

    @Transactional
    public void withdrawById(Long clientId, BigDecimal amount) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found"));
        client.setBalance(client.getBalance().subtract(amount));
        clientRepository.save(client);
    }

    public ClientStatsDTO getClientStats() {
        return clientRepository.getClientStats();
    }

    public void toggleClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Client not found"));
        client.setActive(!client.getActive());
        clientRepository.save(client);

        log.info("Client {} has been toggled", email);
    }

    public Page<ClientDTO> getAllClientsBySearch(String search, Pageable pageable) {
        Page<Client> clientsPage = clientRepository.findAllBySearch(search.toLowerCase(), pageable);
        List<ClientDTO> clientList = clientsPage.stream().map(client -> modelMapper.map(client, ClientDTO.class)).toList();
        return new PageImpl<>(clientList, pageable, clientsPage.getTotalElements());
    }
}

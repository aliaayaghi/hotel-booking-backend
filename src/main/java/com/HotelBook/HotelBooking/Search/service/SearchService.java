package com.HotelBook.HotelBooking.Search.service;


import com.HotelBook.HotelBooking.Common.pagination.PagedResponse;
import com.HotelBook.HotelBooking.Search.dto.SearchRequestDTO;
import com.HotelBook.HotelBooking.Search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchService {

    PagedResponse<SearchResponseDTO> search(SearchRequestDTO dto);

    List<String> autocomplete(String query);
}
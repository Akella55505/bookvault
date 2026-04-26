package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.filter.BookQueryFilter;
import com.epam.rd.autocode.spring.project.dto.filter.QueryFilter;
import com.epam.rd.autocode.spring.project.dto.filter.enums.BookSortByOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaginationHelperService")
public class PaginationHelperServiceTest {
    private PaginationHelperService service;

    @BeforeEach
    void setUp() {
        service = new PaginationHelperService();
    }

    @Nested
    @DisplayName("null QueryFilter")
    class NullFilter {

        @Test
        @DisplayName("returns base URL with no query params")
        void nullFilter() {
            String result = service.buildUrl("/books", null, 0);

            assertThat(result).isEqualTo("/books");
        }

        @Test
        @DisplayName("preserves base URL path exactly")
        void preservesBasePath() {
            String result = service.buildUrl("/employee/orders", null, 2);

            assertThat(result).startsWith("/employee/orders");
            assertThat(result).doesNotContain("?");
        }
    }

    @Nested
    @DisplayName("page parameter")
    class PageParam {

        @Test
        @DisplayName("uses supplied page value, not value from filter")
        void usesSuppliedPage() {
            QueryFilter filter = new BookQueryFilter();
            filter.setPage(0);

            String result = service.buildUrl("/books", filter, 3);

            assertThat(result).contains("page=3");
            assertThat(result).doesNotContain("page=0");
        }

        @ParameterizedTest(name = "page={0}")
        @ValueSource(ints = {0, 1, 5, 99})
        @DisplayName("correctly encodes any page number")
        void anyPageNumber(int page) {
            QueryFilter filter = new BookQueryFilter();
            filter.setPage(0);

            String result = service.buildUrl("/books", filter, page);

            assertThat(result).contains("page=" + page);
        }
    }

    @Nested
    @DisplayName("String field normalisation")
    class StringNormalisation {

        @Test
        @DisplayName("String value is lowercased before appending")
        void lowercasedString() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("Fantasy");

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("search=fantasy");
            assertThat(result).doesNotContain("Fantasy");
        }

        @Test
        @DisplayName("already-lowercase value is unchanged")
        void alreadyLowercase() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("fantasy");

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("search=fantasy");
        }

        @Test
        @DisplayName("mixed-case value is fully lowercased")
        void mixedCase() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("HARRY Potter");

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("search=harry%20potter");
        }
    }

    @Nested
    @DisplayName("null field values")
    class NullFieldValues {

        @Test
        @DisplayName("null String field is omitted from URL")
        void nullStringOmitted() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch(null);

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).doesNotContain("search");
        }

        @Test
        @DisplayName("only non-null fields appear in URL")
        void onlyNonNullFields() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("java");
            filter.setSortBy(null);

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("search=java");
            assertThat(result).doesNotContain("sortBy");
        }
    }

    @Nested
    @DisplayName("non-String field values")
    class NonStringFields {

        @Test
        @DisplayName("non-String value is appended as-is (no lowercasing)")
        void nonStringAppendedAsIs() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setItemsPerPage(20);

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("itemsPerPage=20");
        }
    }

    @Nested
    @DisplayName("multiple fields combined")
    class MultipleFields {

        @Test
        @DisplayName("all non-null fields appear together")
        void allFieldsTogether() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setPage(0);
            filter.setSearch("tolkien");
            filter.setSortBy(BookSortByOptions.NAME);
            filter.setItemsPerPage(10);

            String result = service.buildUrl("/books", filter, 2);

            assertThat(result)
                    .contains("page=2")
                    .contains("search=tolkien")
                    .contains("sortBy=name")
                    .contains("itemsPerPage=10");
        }

        @Test
        @DisplayName("base URL is preserved when params are added")
        void baseUrlPreserved() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("java");

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).startsWith("/books?");
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty string search is included (not null)")
        void emptyStringIncluded() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setSearch("");

            String result = service.buildUrl("/books", filter, 0);

            assertThat(result).contains("search=");
        }

        @Test
        @DisplayName("all-null filter produces only the page param")
        void allNullFilterProducesOnlyPage() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setPage(0);

            String result = service.buildUrl("/books", filter, 1);

            assertThat(result).startsWith("/books?page=1");
        }

        @Test
        @DisplayName("different base URLs produce correctly prefixed results")
        void differentBaseUrls() {
            BookQueryFilter filter = new BookQueryFilter();
            filter.setPage(0);

            assertThat(service.buildUrl("/books", filter, 0)).startsWith("/books");
            assertThat(service.buildUrl("/employee/orders", filter, 0)).startsWith("/employee/orders");
            assertThat(service.buildUrl("/client/orders", filter, 0)).startsWith("/client/orders");
        }
    }
}

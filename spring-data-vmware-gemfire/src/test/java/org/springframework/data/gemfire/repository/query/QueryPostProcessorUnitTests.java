/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.repository.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.QueryMethod;

/**
 * Unit Tests for {@link QueryPostProcessor}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.repository.query.QueryPostProcessor
 * @since 1.0.0
 */
public class QueryPostProcessorUnitTests {

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processAfterReturnsCompositeQueryPostProcessorAndPostProcessesInOrder() {

		QueryMethod mockQueryMethod = mock(QueryMethod.class);

		String query = "SELECT * FROM /Test";

		QueryPostProcessor<Repository, String> mockQueryPostProcessorOne = mock(QueryPostProcessor.class);
		QueryPostProcessor<Repository, String> mockQueryPostProcessorTwo = mock(QueryPostProcessor.class);

		when(mockQueryPostProcessorOne.processAfter(any())).thenCallRealMethod();
		when(mockQueryPostProcessorOne.postProcess(any(QueryMethod.class), anyString(), any())).thenReturn(query);
		when(mockQueryPostProcessorTwo.postProcess(any(QueryMethod.class), anyString(), any())).thenReturn(query);

		QueryPostProcessor<?, String> composite = mockQueryPostProcessorOne.processAfter(mockQueryPostProcessorTwo);

		assertThat(composite).isNotNull();
		assertThat(composite).isNotSameAs(mockQueryPostProcessorOne);
		assertThat(composite).isNotSameAs(mockQueryPostProcessorTwo);
		assertThat(composite.postProcess(mockQueryMethod, query)).isEqualTo(query);

		InOrder inOrder = inOrder(mockQueryPostProcessorOne, mockQueryPostProcessorTwo);

		inOrder.verify(mockQueryPostProcessorTwo, times(1))
			.postProcess(eq(mockQueryMethod), eq(query), any());

		inOrder.verify(mockQueryPostProcessorOne, times(1))
			.postProcess(eq(mockQueryMethod), eq(query), any());
	}

	@Test
	public void processAfterReturnsThis() {

		QueryPostProcessor<?, ?> mockQueryPostProcessor = mock(QueryPostProcessor.class);

		when(mockQueryPostProcessor.processAfter(any())).thenCallRealMethod();

		assertThat(mockQueryPostProcessor.processAfter(null)).isSameAs(mockQueryPostProcessor);
	}
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processBeforeReturnsCompositeQueryPostProcessorAndPostProcessesInOrder() {

		QueryMethod mockQueryMethod = mock(QueryMethod.class);

		String query = "SELECT * FROM /Test";

		QueryPostProcessor<Repository, String> mockQueryPostProcessorOne = mock(QueryPostProcessor.class);
		QueryPostProcessor<Repository, String> mockQueryPostProcessorTwo = mock(QueryPostProcessor.class);

		when(mockQueryPostProcessorOne.processBefore(any())).thenCallRealMethod();
		when(mockQueryPostProcessorOne.postProcess(any(QueryMethod.class), anyString(), any())).thenReturn(query);
		when(mockQueryPostProcessorTwo.postProcess(any(QueryMethod.class), anyString(), any())).thenReturn(query);

		QueryPostProcessor<?, String> composite = mockQueryPostProcessorOne.processBefore(mockQueryPostProcessorTwo);

		assertThat(composite).isNotNull();
		assertThat(composite).isNotSameAs(mockQueryPostProcessorOne);
		assertThat(composite).isNotSameAs(mockQueryPostProcessorTwo);
		assertThat(composite.postProcess(mockQueryMethod, query)).isEqualTo(query);

		InOrder inOrder = inOrder(mockQueryPostProcessorOne, mockQueryPostProcessorTwo);

		inOrder.verify(mockQueryPostProcessorOne, times(1))
			.postProcess(eq(mockQueryMethod), eq(query), any());

		inOrder.verify(mockQueryPostProcessorTwo, times(1))
			.postProcess(eq(mockQueryMethod), eq(query), any());
	}

	@Test
	public void processBeforeReturnsThis() {

		QueryPostProcessor<?, ?> mockQueryPostProcessor = mock(QueryPostProcessor.class);

		when(mockQueryPostProcessor.processBefore(any())).thenCallRealMethod();

		assertThat(mockQueryPostProcessor.processBefore(null)).isSameAs(mockQueryPostProcessor);
	}
}

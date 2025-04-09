/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2025, A. Aquila
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.benkenhobbit.monitoring.demo.service;

import com.github.benkenhobbit.monitoring.demo.controller.DatabaseInterface;
import com.github.benkenhobbit.monitoring.demo.model.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
@EnableAsync
public class NestedMonitorService {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DatabaseInterface databaseInterface;

    /**
     * Constructor injection
     * @param databaseInterface
     */
    public NestedMonitorService(DatabaseInterface databaseInterface) {
        this.databaseInterface = databaseInterface;
    }

    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Future<List<Instrument>> getAsyncInstrumentsNotSupported() {
        List<Instrument> list = databaseInterface.getInstrumentNotSupported(5_000);
        return executor.submit(() -> list);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Instrument> getInstrumentNestedRequiresNew() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public Future<List<Instrument>> getAsyncInstrumentsRequired() {
        List<Instrument> list = databaseInterface.getInstrumentNotSupported(5_000);
        return executor.submit(() -> list);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void getAsyncInstrumentNestedRequiresNew() {
        databaseInterface.getInstrumentNotSupported(5_000);
    }
}

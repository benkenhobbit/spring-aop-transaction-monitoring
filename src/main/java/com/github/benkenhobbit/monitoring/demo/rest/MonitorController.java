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
package com.github.benkenhobbit.monitoring.demo.rest;

import com.github.benkenhobbit.monitoring.demo.service.MonitorService;
import com.github.benkenhobbit.monitoring.model.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("private/transaction-monitor")
@Slf4j
public class MonitorController {

    private final MonitorService transactionMonitorService;

    public MonitorController(MonitorService transactionMonitorService) {
        this.transactionMonitorService = transactionMonitorService;
    }

    @GetMapping("/run-test")
    public ResponseEntity<Void> runTest() {
        transactionMonitorService.executeTest();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }

    @GetMapping("/timeline")
    public List<TransactionEvent> getTimeline() {
        return transactionMonitorService.getEvents();
    }

    @GetMapping("/timeline/{limit}")
    public List<TransactionEvent> getTimeline(@PathVariable int count) {
        return transactionMonitorService.getEvents(count);
    }

    @GetMapping("/print-log")
    public ResponseEntity<Void> printLog() {
        transactionMonitorService.printLog();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }

    @GetMapping("/print-short-log")
    public ResponseEntity<Void> printShortLog() {
        transactionMonitorService.printShortLog();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }

    @GetMapping("/enable")
    public ResponseEntity<Void> enable() {
        transactionMonitorService.enable();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }

    @GetMapping("/disable")
    public ResponseEntity<Void> disable() {
        transactionMonitorService.disable();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }

    @GetMapping("/clear")
    public ResponseEntity<Void> clear() {
        transactionMonitorService.clearAll();
        return ResponseEntity.ok().build(); // Returns 200 OK
    }
}

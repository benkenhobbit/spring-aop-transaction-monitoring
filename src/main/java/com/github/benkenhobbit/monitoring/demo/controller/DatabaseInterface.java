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
package com.github.benkenhobbit.monitoring.demo.controller;

import com.github.benkenhobbit.monitoring.demo.model.Instrument;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake controller used for demo scope only.
 */
@Controller
public class DatabaseInterface {

    //@PersistenceContext
    //protected EntityManager em;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Instrument> getInstrumentNotSupported(double... wait) {
        //Replace with your useful code

        List<Instrument> list = new ArrayList<>();
        int max = (int)(Math.random()*10);
        for (int i = 0; i < max; i++) {
            double random = Math.random();
            list.add(new Instrument(random < 0.25 ? "Guitar" :
                    random < 0.5 ? "Piano" :
                            random < 0.75 ? "Violin" : "Sax"));
        }

        try {
            double d = wait.length >= 1 ? wait[0] : 0;
            Thread.sleep((long)(Math.random()*500 + d));
        } catch (InterruptedException e) {}
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Instrument> getInstrumentRequired(double... wait) {
        //Replace with your useful code

        List<Instrument> list = new ArrayList<>();
        int max = (int)(Math.random()*10);
        for (int i = 0; i < max; i++) {
            double random = Math.random();
            list.add(new Instrument(random < 0.25 ? "Guitar" :
                    random < 0.5 ? "Piano" :
                            random < 0.75 ? "Violin" : "Sax"));
        }

        try {
            double d = wait.length >= 1 ? wait[0] : 0;
            Thread.sleep((long)(Math.random()*500 + d));
        } catch (InterruptedException e) {}
        return new ArrayList<>();
    }

}

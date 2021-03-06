// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * This class reads the output from a launched process and writes it to stdout.
 */
public class StreamEater extends Thread {
    private InputStream stream;

    public StreamEater(InputStream stream) {
        this.stream = new BufferedInputStream(stream);
    }

    public void run() {
        try {
            StringBuilder s = new StringBuilder();
            while (true) {
                int c = stream.read();
                if (c == -1){
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, s.toString());
                    break;
                } else {
                    char ch = (char) c;
                    if (ch == '\n'){
                        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, s.toString());
                        s = new StringBuilder();
                    }else {
                        s.append((char) c);
                    }
                }
                
            }
        } catch (IOException ex) {
        }
    }
}

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;

public class Sud2SatExtended
{
	public static void main(String[] args) {
        if (args.length<2) {
            System.err.println("Not enough args");
            return;
        }
        String filename = args[0];
        String outfile = args[1];
        String tmp = null;
        StringBuffer boardString = new StringBuffer();

        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            while ((tmp = br.readLine()) != null) {
                if (tmp.startsWith("Grid")) {
                    tmp = br.readLine();
                }
                String line = tmp.trim();
                line = line.replace("\n", "");
                line = line.replaceAll("[?.*]", "0");
                boardString.append(line);
            }

            int[] board = new int[boardString.length()];
            for (int i=0; i<boardString.length(); i++) {
                board[i] = boardString.charAt(i) - '0';
            }
            int size = (int) Math.sqrt(boardString.length());

            int clause = 0;
            ArrayList<String> dimacs = new ArrayList<String>();

            dimacs.add("c Given Board Requirements");
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    int index = getIndex(i,j,size);
                    int val = board[index];
                    if (val !=0) {
                        val = toBase(i+1, j+1, val, size);
                        clause++;
                        dimacs.add(val + " 0");
                    }
                }
            }

            dimacs.add("c One number per entry");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    String di ="";
                    for (int k=1; k<size+1; k++) {
                        int val = toBase(i,j,k,size);
                        di += val + " ";
                    }
                    dimacs.add(di + "0");
                    clause++;
                }
            }

            dimacs.add("c Row constraint");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    for (int k=1; k<size+1; k++) {
                        for (int js=j+1; js<size+1; js++) {
                            int val1 = toBase(i,j,k,size);
                            int val2 = toBase(i,js,k,size);
                            dimacs.add("-"+val1+" "+"-"+val2+" 0");
                            clause++;
                        }
                    }
                }
            }

            dimacs.add("c Column constraint");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    for (int k=1; k<size+1; k++) {
                        for (int is=i+1; is<size+1; is++) {
                            int val1 = toBase(i,j,k,size);
                            int val2 = toBase(is,j,k,size);
                            dimacs.add("-"+val1+" "+"-"+val2+" 0");
                            clause++;
                        }
                    }
                }
            }

            dimacs.add("c 3X3 constraint");
            int blocksize = (int) Math.sqrt(size);
            for (int k=1; k<size+1; k++) {
                for (int a=0; a<blocksize; a++) {
                    for (int b=0; b<blocksize; b++) {
                        for (int u=1; u<blocksize+1; u++) {
                            for (int v=1; v<blocksize; v++) {
                                for (int w=v+1; w<blocksize+1; w++) {
                                    int val1 = toBase(blocksize*a +u, blocksize*b+v, k, size);
                                    int val2 = toBase(blocksize*a +u, blocksize*b+w, k, size);
                                    dimacs.add("-"+val1+" "+"-"+val2+" 0");
                                    clause++;
                                }
                            }
                        }
                    }
                }
            }

            for (int k=1; k<size+1; k++) {
                for (int a=0; a<blocksize; a++) {
                    for (int b=0; b<blocksize; b++) {
                        for (int u=1; u<blocksize; u++) {
                            for (int v=1; v<blocksize+1; v++) {
                                for (int w=u+1; w<blocksize+1; w++) {
                                    for (int x=1; x<blocksize+1; x++) {
                                        int val1 = toBase(blocksize*a +u, blocksize*b+v, k, size);
                                        int val2 = toBase(blocksize*a +w, blocksize*b+x, k, size);
                                        dimacs.add("-"+val1+" "+"-"+val2+" 0");
                                        clause++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Extended Rule
            dimacs.add("c At most one number in each entry");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    for (int k=1; k<size; k++) {
                        for (int l=k+1; l<size+1; l++) {
                            int val1 = toBase(i, j, k, size);
                            int val2 = toBase(i, j, l, size);
                            dimacs.add("-"+val1+" "+"-"+val2+" 0");
                            clause++;
                        }
                    }
                }
            }

            //Extended Rule
            dimacs.add("c Each number appears at least once in each row");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    StringBuffer rule = new StringBuffer();
                    for (int k=1; k<size+1; k++) {
                        int val1 = toBase(i, j, k, size);
                        rule.append(val1 + " ");
                    }
                    dimacs.add(rule.toString() + "0");
                    clause++;
                }
            }

            //Extended Rule
            dimacs.add("c Each number appears at least once in each column");
            for (int i=1; i<size+1; i++) {
                for (int j=1; j<size+1; j++) {
                    StringBuffer rule = new StringBuffer();
                    for (int k=1; k<size+1; k++) {
                        int val1 = toBase(i, j, k, size);
                        rule.append(val1 + " ");
                    }
                    dimacs.add(rule.toString() + "0");
                    clause++;
                }
            }


            //Extended Rule
            dimacs.add("c Each number appears at least once in each 3X3 subgrid");
            for (int i=0; i<blocksize; i++) {
                for (int j=0; j<blocksize; j++) {
                    for (int z=1; z<size+1; z++) {
                        StringBuffer rule = new StringBuffer();
                        for (int x=1; x<blocksize+1; x++) {
                            for (int y=1; y<blocksize+1; y++) {
                                int val = toBase(blocksize*i + x,
                                                blocksize*j + y,
                                                z, size);
                                rule.append(val + " ");
                            }
                        }
                        dimacs.add(rule.toString() + '0');
                        clause++;
                    }
                }
            }

            String header = "p cnf " + size*size*size + " " + clause;
            dimacs.add(0, header);
            Path file = Paths.get(outfile);
            Files.write(file, dimacs, Charset.forName("UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }

	}

    public static int getIndex(int i, int j, int size) {
        return i + size * j;
    }

    public static int toBase(int i, int j, int k, int size) {
        return size*size * (i-1) + size * (j-1) + (k-1) + 1;
    }
}

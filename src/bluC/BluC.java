package bluC;

import bluC.transpiler.Transpiler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;

/**
 *
 * @author John Schneider
 */
public class BluC
{
    public static final String BLU_C_NAMESPACE_PREFIX = "___bluC";
    private static Transpiler transpiler;
    private static long transpileStartTime = -1;
    private static long transpileEndTime = -1;
    private static long compileEndTime = -1;
    
    private static Token getNullTokenWithFilePath(String filePath)
    {
        return new Token(
            new TokenInfo(Token.NO_TEXT_CONTENT, true),

            new TokenFileInfo(filePath, 
                TokenFileInfo.NO_LINE_INDEX));
    }
    
    public static ArrayList<String> readFile(File theFile)
    {
        ArrayList<String> returnee = new ArrayList<>();
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(theFile));
            String line = br.readLine();
            
            while (line != null)
            {
                returnee.add(line);
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return returnee;
    }
    
    public static void writeFile(String fileName, ArrayList<String> contents)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                fileName)));
            
            for (int i = 0; i < contents.size(); i++)
            {
                bw.append(contents.get(i) + "\n");
            }
            
            bw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }
    
    private static void printfile(
        String prefix, ArrayList<String> rawFileContents)
    {
        System.out.println(prefix);
        for (String s: rawFileContents)
        {
            System.out.println("\t" + s);
        }
        System.out.println();
    }
    
    private static void compileExistingFile(
        String[] args, File f)
    {   
        ArrayList<String> rawFileContents = readFile(f);
        
        try
        {
            transpiler = new Transpiler(f.getCanonicalPath(), rawFileContents);
            
            rawFileContents = transpiler.transpile();
            
            if (Flags.get("time") != null)
            {
                transpileEndTime = System.currentTimeMillis();
            }

            String filePathWithoutExtension = args[0].substring(0, 
                args[0].indexOf("."));
            String fileNameWithoutExtension = filePathWithoutExtension.
                substring(filePathWithoutExtension.lastIndexOf("/") + 1, 
                filePathWithoutExtension.length());
            String outputCFileName = filePathWithoutExtension + ".c";
            
            writeFile(outputCFileName, rawFileContents);
            
            if (!Logger.hasLoggedError())
            {
                if (Flags.get("c") == null || Flags.get("exe") != null)
                {
                    try
                    {
                        Process p;
                        p = Runtime.getRuntime().exec(new String[]{"gcc", 
                            outputCFileName, "-o", fileNameWithoutExtension});

                        try
                        {
                            p.waitFor();
                        } catch (InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }

                        compileEndTime = System.currentTimeMillis();

                        Debug.out.println("[BluC.main]: ran " + "gcc \"" + 
                            outputCFileName + "\" -o " + 
                            fileNameWithoutExtension);

                        BufferedReader input = new BufferedReader(new 
                            InputStreamReader(p.getInputStream()));
                        BufferedReader err = new BufferedReader(new 
                            InputStreamReader(p.getErrorStream()));

                        try 
                        {
                            String line;

                            System.out.println("\n\ngcc output:");
                            line = input.readLine();
                            while (line != null)
                            {
                                System.out.println(line);
                                line = input.readLine();
                            }
                            input.close();

                            line = err.readLine();
                            while (line != null)
                            {
                                System.out.println(line);
                                line = err.readLine();
                            }
                            err.close();

                            if (Flags.get("c") == null)
                            {
                                File outputCFile = new File(outputCFileName);
                                outputCFile.delete();
                            }
                        } catch (IOException e) 
                        {
                            e.printStackTrace();
                        }

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException ex)
        {
            Logger.err(
                getNullTokenWithFilePath(f.getAbsolutePath()),
                "FATAL: Cannot resolve canonical path: ");
            
            ex.printStackTrace();
        }
    }
    
    private static void letJVMSpinUp()
    {
        try 
        {
            // let the JVM spin up so we get a more accurate measurement

            System.out.println("Letting the JVM spin-up so more accurate " +
                "timing can be recorded.");
            
            for (int i = 3; i > 0; i--)
            {
                System.out.println(i);
                Thread.sleep(1000);
            }
            System.out.println("Spinup done.\n");
            
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public static void compile(String[] args)
    {
        if (Flags.get("time") != null)
        {
            letJVMSpinUp();
            transpileStartTime = System.currentTimeMillis();
        }
        
        File f = new File(args[0]);
        
        if (f.exists())
        {
            compileExistingFile(args, f);
        }
        else
        {
            try
            {
                Logger.err(
                    getNullTokenWithFilePath(f.getCanonicalPath()),
                    "FATAL: file `" + args[0] + "` not found");
            } catch (IOException ex)
            {
                Token filePath = getNullTokenWithFilePath(f.getAbsolutePath());
                Logger.err(filePath, "FATAL: file `" + args[0] + "` not found");
                
                Logger.err(filePath, "FATAL: Cannot resolve canonical path: ");
                ex.printStackTrace();
            }
        }
        
        if (Flags.get("time") != null && !Logger.hasLoggedError())
        {
            System.out.println("\n\nTranspilation done in " + 
                (transpileEndTime - transpileStartTime) + " ms.");
        }
    }
    
    private static void printHelp()
    {
        System.out.println("This program compiles a BluC file. The first " +
            "argument *must* be the file path with the extension of the BluC " +
            "source file. If no arguments are provided, this help message is " +
            "displayed." +
                
            "\n\nUSAGE:\n" +
            "    bluc filePathHere.extension -aFlagHere -aSecondFlag" +
                
            "\n\nFLAGS:\n" +
            "    -parseTree : outputs the parse tree of the file\n" +
            "    -c : outputs the transpiled c file instead of compiling to " +
            "an exe\n" +
            "    -exe : used in conjunction with -c flag to output both a .c " +
            "and exe file" +
            "    -time : outputs the number of milliseconds compilation took");
    }
    
    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            String s = args[i].toLowerCase();
            
            if (s.equals("-parsetree"))
            {
                Flags.set("parseTree", "true");
            }
            else if (s.equals("-c"))
            {
                Flags.set("c", "true");
            }
            else if (s.equals("-exe"))
            {
                Flags.set("exe", "true");
            }
            else if (s.equals("-time"))
            {
                Flags.set("time", "true");
            }
        }
        
        if (args.length == 0)
        {
            printHelp();
        }
        else
        {
            //else just track transpile time
            if (Flags.get("time") != null && 
                (Flags.get("c") == null || Flags.get("exe") != null))
            {
                compile(args);
                
                if (!Logger.hasLoggedError())
                {
                    System.out.println("Compilation done in " + 
                        (compileEndTime - transpileEndTime) + " ms");
                    System.out.println("Total time elapsed is " + 
                        (compileEndTime - transpileStartTime) + " ms");
                }
            }
            else
            {
                compile(args);
            }
        }
    }
}

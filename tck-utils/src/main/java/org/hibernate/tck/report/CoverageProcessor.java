package org.hibernate.tck.report;import java.io.File;import java.io.FileInputStream;import java.util.ArrayList;import java.util.List;import org.hibernate.tck.annotations.SpecAssertion;import com.sun.mirror.apt.AnnotationProcessor;import com.sun.mirror.apt.AnnotationProcessorEnvironment;import com.sun.mirror.declaration.AnnotationTypeDeclaration;import com.sun.mirror.declaration.Declaration;import com.sun.mirror.declaration.MethodDeclaration;import com.sun.mirror.util.DeclarationVisitors;import com.sun.mirror.util.SimpleDeclarationVisitor;/** * Annotation processor for generating TCK coverage report *  * @author Shane Bryzak */public class CoverageProcessor implements AnnotationProcessor{   private static final String OUTDIR_OPTION_NAME = "-s";   private static final String REPORT_FILE_NAME = "coverage.html";       private static final String AUDIT_FILE_NAME = "tck-audit.xml";         private final AnnotationProcessorEnvironment env;   private final File baseDir;      private final List<SpecReference> references = new ArrayList<SpecReference>();      private AuditParser auditParser;      public CoverageProcessor(AnnotationProcessorEnvironment env)   {      this.env = env;      String baseDirName = env.getOptions().get( OUTDIR_OPTION_NAME );      baseDir = new File( baseDirName );      baseDir.mkdirs();            try      {         auditParser = new AuditParser(new FileInputStream(AUDIT_FILE_NAME));         auditParser.parse();      }      catch (Exception ex)      {         throw new RuntimeException("Error parsing tck-audit.xml: " +                ex.getClass().getName() + " - " + ex.getMessage(), ex);      }   }   public void process()   {      AnnotationTypeDeclaration annotationType = (AnnotationTypeDeclaration)          env.getTypeDeclaration(SpecAssertion.class.getCanonicalName());            for (Declaration d : env.getDeclarationsAnnotatedWith(annotationType))      {         d.accept(DeclarationVisitors.getDeclarationScanner(               new CreateReferenceVisitor(), DeclarationVisitors.NO_OP));      }            annotationType = (AnnotationTypeDeclaration)       env.getTypeDeclaration(SpecAssertions.class.getCanonicalName());      for (Declaration d : env.getDeclarationsAnnotatedWith(annotationType))      {         d.accept(DeclarationVisitors.getDeclarationScanner(               new CreateReferenceVisitor(), DeclarationVisitors.NO_OP));      }                        new CoverageReport(references, auditParser).writeToFile(new File(baseDir, REPORT_FILE_NAME));   }           private class CreateReferenceVisitor extends SimpleDeclarationVisitor    {      public void visitMethodDeclaration(MethodDeclaration d)       {         SpecAssertions assertions = d.getAnnotation ( SpecAssertions.class );         if (assertions != null)         {            for (SpecAssertion assertion : assertions.value())            {               SpecReference ref = new SpecReference(                     assertion.section(), assertion.id(),                      d.getDeclaringType().getSimpleName(), d.getSimpleName());                references.add( ref );                           }         }                  SpecAssertion assertion = d.getAnnotation( SpecAssertion.class );         if (assertion != null)         {            SpecReference ref = new SpecReference(                  assertion.section(), assertion.id(),                   d.getDeclaringType().getSimpleName(), d.getSimpleName());             references.add( ref );         }      }   }   }
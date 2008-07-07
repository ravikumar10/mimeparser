SRC= Makefile t.tex praca.tex t.bib

# pdf, ps, gs, gs2, dvi
default: pdf

pdf: t.pdf
	acroread t.pdf
    
ps: t.ps

gs: t.ps
	gv -scale -2 -geometry +0+0 t.ps

gs2: t.ps
	psnup -2 t.ps > tt.ps
	gv -landscape -scale -1 -geometry +0+0 tt.ps

dvi: t.dvi
	@xdvi + -s 4 -topmargin 6cm -bg white -keep -geometry 715x724+0+0 t

ed:
	vim t.tex

t: touch default

touch:
	touch t.tex

t.dvi: $(SRC)
#	@( cd fig; make all )
	latex t
	bibtex t 
	latex t > /dev/null
	latex t > /dev/null

t.pdf: t.dvi
	dvipdf t.dvi

t.ps: t.dvi
	dvips t.dvi -o t.ps

#tgz: clean
#	( D=`basename $$PWD`; cd ..; tar czf $$D.tgz $$D; ls -l $$D.tgz )

aspell:
	aspell --lang=polish -c praca.tex

clean:
	rm -f *.dvi *.bbl *.blg *.log *.toc *.aux *.ps *.lof *.pdf

	.text
Compare.LT:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,Compare.LT.y
	movq %rdi,Compare.LT.x
L.12:
	xorq t.0,t.0
	cmpq Compare.LT.y,Compare.LT.x
	jl L.1
L.2:
	movq t.0,%rax
	jmp L.0
L.1:
	movq $1,t.0
	jmp L.2
L.0:
#	returnSink
	popq %rbp
	ret
Compare.LT.badSub:
	call badSub
	.text
Compare.LE:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,Compare.LE.y
	movq %rdi,Compare.LE.x
L.13:
	xorq t.1,t.1
	cmpq Compare.LE.y,Compare.LE.x
	jle L.4
L.5:
	movq t.1,%rax
	jmp L.3
L.4:
	movq $1,t.1
	jmp L.5
L.3:
#	returnSink
	popq %rbp
	ret
Compare.LE.badSub:
	call badSub
	.text
Compare.GT:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,Compare.GT.y
	movq %rdi,Compare.GT.x
L.14:
	xorq t.2,t.2
	cmpq Compare.GT.y,Compare.GT.x
	jg L.7
L.8:
	movq t.2,%rax
	jmp L.6
L.7:
	movq $1,t.2
	jmp L.8
L.6:
#	returnSink
	popq %rbp
	ret
Compare.GT.badSub:
	call badSub
	.text
Compare.GE:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,Compare.GE.y
	movq %rdi,Compare.GE.x
L.15:
	xorq t.3,t.3
	cmpq Compare.GE.y,Compare.GE.x
	jge L.10
L.11:
	movq t.3,%rax
	jmp L.9
L.10:
	movq $1,t.3
	jmp L.11
L.9:
#	returnSink
	popq %rbp
	ret
Compare.GE.badSub:
	call badSub
	.text
Compare:
	pushq %rbp
	movq %rsp,%rbp
L.16:
#	returnSink
	popq %rbp
	ret

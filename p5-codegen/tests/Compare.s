	.text
Compare.LT:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rbx
L.12:
	xorq %rax,%rax
#	movq %rax,%rax
	cmpq %rcx,%rbx
	jl L.1
L.2:
#	movq %rax,%rax
	jmp L.0
L.1:
	movq $1,%rax
#	movq %rax,%rax
	jmp L.2
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
Compare.LE:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rbx
L.13:
	xorq %rax,%rax
#	movq %rax,%rax
	cmpq %rcx,%rbx
	jle L.4
L.5:
#	movq %rax,%rax
	jmp L.3
L.4:
	movq $1,%rax
#	movq %rax,%rax
	jmp L.5
L.3:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
Compare.GT:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rbx
L.14:
	xorq %rax,%rax
#	movq %rax,%rax
	cmpq %rcx,%rbx
	jg L.7
L.8:
#	movq %rax,%rax
	jmp L.6
L.7:
	movq $1,%rax
#	movq %rax,%rax
	jmp L.8
L.6:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
Compare.GE:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rbx
L.15:
	xorq %rax,%rax
#	movq %rax,%rax
	cmpq %rcx,%rbx
	jge L.10
L.11:
#	movq %rax,%rax
	jmp L.9
L.10:
	movq $1,%rax
#	movq %rax,%rax
	jmp L.11
L.9:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
Compare:
	pushq %rbp
	movq %rsp,%rbp
L.16:
#	returnSink
	popq %rbp
	ret

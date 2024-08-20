	.data
	.balign 8
SubrangeFields.r:
	.zero 8
	.text
SubrangeFields.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.11:
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl L.1
L.2:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.5
L.6:
	movq %rbp,%r10
	movq %rbx,%rdi
	call SubrangeFields.println.1.f
#	movq %rax,%rax
L.7:
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	jmp L.0
L.1:
	movq $45,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
	subq %rbx,%rax
	movq %rax,%rbx
	jmp L.2
L.5:
	movq $48,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	jmp L.7
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
SubrangeFields.println.badSub:
	call badSub
	.text
SubrangeFields.println.1.f:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %r10,%rcx
	movq %rdi,%rbx
L.12:
	xorq %rax,%rax
	cmpq %rax,%rbx
	jg L.9
L.10:
	jmp L.8
L.9:
	movq %rcx,%r10
	movq $10,%rcx
	movq %rbx,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
	movq %rcx,%rdi
	call SubrangeFields.println.1.f
#	movq %rax,%rax
	movq $10,%rcx
	movq %rbx,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
#	movq %rcx,%rcx
	imulq $10,%rcx
	movq %rbx,%rax
	subq %rcx,%rax
#	movq %rax,%rax
	addq $48,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	jmp L.10
L.8:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
SubrangeFields.println.1.f.badSub:
	call badSub
	.text
SubrangeFields:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.13:
	leaq SubrangeFields.r(%rip),%rbx
	movq $255,%rax
	movq %rax,0(%rbx)
	leaq SubrangeFields.r(%rip),%rbx
	movq $-1,%rax
	movq %rax,1(%rbx)
	leaq SubrangeFields.r(%rip),%rax
	movzbq 0(%rax),%rax
	movq %rax,%rdi
	call SubrangeFields.println
#	movq %rax,%rax
	leaq SubrangeFields.r(%rip),%rax
	movsbq 1(%rax),%rax
	movq %rax,%rdi
	call SubrangeFields.println
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
SubrangeFields.badSub:
	call badSub

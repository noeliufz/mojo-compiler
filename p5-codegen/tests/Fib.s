	.text
Fib.fib:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.33:
	movq $2,%rax
	cmpq %rax,%rbx
	jl L.1
L.2:
	movq %rbx,%rax
	subq $1,%rax
	movq %rax,%rdi
	call Fib.fib
	movq %rax,%r12
	movq %rbx,%rax
	subq $2,%rax
	movq %rax,%rdi
	call Fib.fib
	movq %rax,%rbx
	movq %r12,%rax
	addq %rbx,%rax
#	movq %rax,%rax
	jmp L.0
L.1:
	movq %rbx,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
Fib.fib.badSub:
	call badSub
	.text
Fib.pow:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %rbx, -8(%rbp)
#	movq %rsi,%rsi
	movq %rdi,%rbx
L.34:
	xorq %rax,%rax
	cmpq %rax,%rsi
	je L.4
L.5:
	movq $2,%rcx
	movq %rsi,%rax
	cqto
	idivq %rcx
	movq %rdx,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.7
L.8:
	movq %rsi,%rax
	subq $1,%rax
	movq $2,%rcx
	movq %rax,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
#	movq %rcx,%rcx
	movq %rbx,%rax
	imulq %rbx,%rax
	movq %rax,%rdi
	movq %rcx,%rsi
	call Fib.pow
	movq %rax,%rcx
	movq %rbx,%rax
	imulq %rcx,%rax
#	movq %rax,%rax
	jmp L.3
L.4:
	movq $1,%rax
#	movq %rax,%rax
	jmp L.3
L.35:
L.6:
	xorq %rax,%rax
#	movq %rax,%rax
	subq $1,%rax
#	movq %rax,%rax
	jmp L.3
L.7:
	movq %rbx,%rax
	imulq %rbx,%rax
	movq %rax,%rdi
	movq $2,%rbx
	movq %rsi,%rax
	cqto
	idivq %rbx
	movq %rax,%rbx
	movq %rbx,%rsi
	call Fib.pow
#	movq %rax,%rax
	jmp L.3
L.36:
L.9:
	jmp L.6
L.3:
#	returnSink
	movq -8(%rbp),%rbx
	addq $32,%rsp
	popq %rbp
	ret
Fib.pow.badSub:
	call badSub
	.text
Fib.read_number:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
L.37:
	xorq %rax,%rax
	movq %rax,%r12
L.13:
L.14:
	call getchar
	movq %rax,%rbx
	movq $10,%rax
	cmpq %rax,%rbx
	je L.16
L.18:
	movq $13,%rax
	cmpq %rax,%rbx
	je L.16
L.17:
	movq %r12,%rax
	imulq $10,%rax
#	movq %rbx,%rbx
	subq $48,%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%r12
	jmp L.13
L.16:
L.15:
	movq %r12,%rax
L.12:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $16,%rsp
	popq %rbp
	ret
Fib.read_number.badSub:
	call badSub
	.text
Fib.print_number:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,%r12
L.38:
	movq %r12,%rdi
	xorq %rax,%rax
	movq %rax,%rbx
L.20:
L.21:
	movq $10,%rcx
	movq %rdi,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
	movq %rcx,%rdi
	movq %rbx,%rax
	addq $1,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rdi
	jne L.20
L.22:
L.23:
L.24:
	xorq %rax,%rax
	movq %rax,%rsi
L.26:
L.27:
L.28:
	movq %r12,%rdi
	movq %rbx,%rax
	subq $1,%rax
	movq %rax,%rbx
L.29:
	cmpq %rbx,%rsi
	jne L.30
L.31:
	movq $10,%rcx
	movq %rdi,%rax
	cqto
	idivq %rcx
	movq %rdx,%rax
#	movq %rax,%rax
	addq $48,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	xorq %rax,%rax
	cmpq %rax,%rbx
	jne L.23
L.25:
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	jmp L.19
L.39:
	jmp L.26
L.30:
	movq %rsi,%rax
	addq $1,%rax
	movq %rax,%rsi
	movq $10,%rcx
	movq %rdi,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
	movq %rcx,%rdi
	jmp L.29
L.19:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
Fib.print_number.badSub:
	call badSub
	.text
Fib.exit_print:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.40:
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $69,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $120,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $105,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $116,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $105,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $110,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $103,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $46,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $46,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $46,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	xorq %rax,%rax
	movq %rax,%rdi
	call exit
#	movq %rax,%rax
L.32:
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
Fib.exit_print.badSub:
	call badSub
	.text
Fib:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.41:
	call Fib.read_number
#	movq %rax,%rax
	movq %rax,%rdi
	call Fib.fib
#	movq %rax,%rax
	movq %rax,%rdi
	call Fib.print_number
#	movq %rax,%rax
	call Fib.exit_print
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
Fib.badSub:
	call badSub
